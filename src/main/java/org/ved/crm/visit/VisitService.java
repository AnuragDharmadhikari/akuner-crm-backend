package org.ved.crm.visit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.common.exception.ResourceNotFoundException;
import org.ved.crm.doctor.Doctor;
import org.ved.crm.doctor.DoctorRepository;
import org.ved.crm.inventory.Batch;
import org.ved.crm.inventory.BatchRepository;
import org.ved.crm.inventory.InventoryService;
import org.ved.crm.product.Product;
import org.ved.crm.product.ProductRepository;
import org.ved.crm.target.CallTargetRepository;
import org.ved.crm.user.User;
import org.ved.crm.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitService {

    private final VisitRepository visitRepository;
    private final VisitProductRepository visitProductRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final InventoryService inventoryService;
    private final CallTargetRepository callTargetRepository;
    private final VisitMapper visitMapper;

    public List<VisitDto> getAllVisits() {
        return visitRepository.findAllWithDetails()
                .stream()
                .map(visitMapper::toDto)
                .toList();
    }

    public VisitDto getVisitById(UUID id) {
        Visit visit = visitRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit", "id", id));
        return visitMapper.toDto(visit);
    }

    public List<VisitDto> getVisitsByDoctor(UUID doctorId) {
        return visitRepository.findByDoctorIdWithDetails(doctorId)
                .stream()
                .map(visitMapper::toDto)
                .toList();
    }

    public List<VisitDto> getVisitsByRep(UUID repId) {
        return visitRepository.findByRepIdWithDetails(repId)
                .stream()
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional
    public VisitDto createVisit(CreateVisitRequest request) {

        // Validate rep
        User rep = userRepository.findById(request.repId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.repId()));
        if (!rep.isActive()) {
            throw new IllegalArgumentException(
                    "Rep is deactivated and cannot log visits: "
                            + rep.getFullName());
        }

        // Validate doctor
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor", "id", request.doctorId()));
        if (!doctor.isActive()) {
            throw new IllegalArgumentException(
                    "Doctor is deactivated and cannot be visited: "
                            + doctor.getFullName());
        }

        // Build and save visit first so it has an ID
        // before visit products reference it
        Visit visit = Visit.builder()
                .rep(rep)
                .doctor(doctor)
                .visitDate(request.visitDate())
                .status(request.status())
                .notes(request.notes())
                .build();

        Visit saved = visitRepository.save(visit);

        // Build visit products, deduct samples from stock
        if (request.products() != null && !request.products().isEmpty()) {
            List<VisitProduct> visitProducts =
                    buildVisitProducts(request.products(), saved);
            visitProductRepository.saveAll(visitProducts);
            saved.setVisitProducts(visitProducts);

            for (VisitProduct vp : visitProducts) {
                if (vp.getSamplesGiven() != null && vp.getSamplesGiven() > 0) {
                    inventoryService.deductStockForSample(
                            vp.getBatch().getId(),
                            vp.getSamplesGiven(),
                            saved.getId()
                    );
                }
            }
        }

        // Re-fetch BEFORE incrementActualVisits — clearAutomatically = true
        // on the @Modifying query clears the persistence context, which would
        // cause findByIdWithDetails to return empty if called after.
        VisitDto result = visitMapper.toDto(
                visitRepository.findByIdWithDetails(saved.getId()).orElseThrow());

        if (request.status() == VisitStatus.COMPLETED) {
            callTargetRepository.incrementActualVisits(
                    rep.getId(),
                    request.visitDate().getMonthValue(),
                    request.visitDate().getYear()
            );
        }

        return result;
    }

    @Transactional
    public VisitDto updateVisit(UUID id, UpdateVisitRequest request) {

        Visit visit = visitRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Visit", "id", id));

        VisitStatus previousStatus = visit.getStatus();

        if (request.status() != null) {
            visit.setStatus(request.status());
        }

        if (request.notes() != null) {
            visit.setNotes(request.notes());
        }

        if (request.products() != null) {
            visit.getVisitProducts().clear();

            List<VisitProduct> newProducts =
                    buildVisitProducts(request.products(), visit);
            visit.getVisitProducts().addAll(newProducts);

            // NOTE: Sample stock deduction is intentionally NOT performed on update.
            // Samples were already deducted on createVisit.
            // Allowing re-deduction on update would cause double stock reduction.
            // If the rep gave additional samples on a follow-up, log a new visit.
        }

        Visit saved = visitRepository.save(visit);

        // Re-fetch BEFORE incrementActualVisits — same reason as createVisit.
        // clearAutomatically = true clears persistence context after the UPDATE,
        // so we must build the response DTO first.
        VisitDto result = visitMapper.toDto(
                visitRepository.findByIdWithDetails(saved.getId()).orElseThrow());

        if (request.status() == VisitStatus.COMPLETED
                && previousStatus != VisitStatus.COMPLETED) {
            callTargetRepository.incrementActualVisits(
                    visit.getRep().getId(),
                    visit.getVisitDate().getMonthValue(),
                    visit.getVisitDate().getYear()
            );
        }

        return result;
    }

    private List<VisitProduct> buildVisitProducts(
            List<VisitProductRequest> requests, Visit visit) {

        List<VisitProduct> result = new ArrayList<>();

        for (VisitProductRequest req : requests) {

            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", req.productId()));
            if (!product.isActive()) {
                throw new IllegalArgumentException(
                        "Product is deactivated and cannot be detailed: "
                                + product.getName());
            }

            Batch batch = null;
            if (req.samplesGiven() != null && req.samplesGiven() > 0) {

                if (req.batchId() == null) {
                    throw new IllegalArgumentException(
                            "batchId is required when samplesGiven > 0 for product: "
                                    + product.getName());
                }

                batch = batchRepository.findByIdWithDetails(req.batchId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Batch", "id", req.batchId()));

                if (batch.isExpired()) {
                    throw new IllegalArgumentException(
                            "Cannot distribute samples from expired batch: "
                                    + batch.getBatchNumber());
                }

                if (!batch.getProduct().getId().equals(product.getId())) {
                    throw new IllegalArgumentException(
                            "Batch " + batch.getBatchNumber()
                                    + " does not belong to product: "
                                    + product.getName());
                }
            }

            result.add(VisitProduct.builder()
                    .visit(visit)
                    .product(product)
                    .batch(batch)
                    .samplesGiven(req.samplesGiven())
                    .feedback(req.feedback())
                    .build());
        }
        return result;
    }
}