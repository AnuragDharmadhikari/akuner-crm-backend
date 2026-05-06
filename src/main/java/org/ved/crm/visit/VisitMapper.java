package org.ved.crm.visit;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class VisitMapper {

    public VisitDto toDto(Visit visit) {
        List<VisitProductDto> productDtos = visit.getVisitProducts()
                .stream()
                .map(this::toVisitProductDto)
                .toList();

        return new VisitDto(
                visit.getId(),
                visit.getRep().getId(),
                visit.getRep().getFullName(),
                visit.getDoctor().getId(),
                visit.getDoctor().getFullName(),
                visit.getDoctor().getSpecialty(),
                visit.getVisitDate(),
                visit.getStatus(),
                visit.getNotes(),
                visit.getAiSummary(),
                productDtos,
                visit.getCreatedAt(),
                visit.getUpdatedAt()
        );
    }

    private VisitProductDto toVisitProductDto(VisitProduct vp) {

        // Batch is nullable — rep may pitch without giving samples.
        // Null-check before accessing batch fields.
        UUID batchId = vp.getBatch() != null
                ? vp.getBatch().getId()
                : null;

        String batchNumber = vp.getBatch() != null
                ? vp.getBatch().getBatchNumber()
                : null;

        return new VisitProductDto(
                vp.getId(),
                vp.getProduct().getId(),
                vp.getProduct().getName(),
                vp.getProduct().getHsnCode(),
                batchId,
                batchNumber,
                vp.getSamplesGiven(),
                vp.getFeedback()
        );
    }
}