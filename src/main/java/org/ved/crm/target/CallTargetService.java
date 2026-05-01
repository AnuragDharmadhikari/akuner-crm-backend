package org.ved.crm.target;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.common.exception.ResourceNotFoundException;
import org.ved.crm.user.User;
import org.ved.crm.user.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallTargetService {

    private final CallTargetRepository callTargetRepository;
    private final UserRepository userRepository;
    private final CallTargetMapper callTargetMapper;

    public List<CallTargetDto> getTargetsByRep(UUID repId){
        return callTargetRepository.findByRepIdWithDetails(repId)
                .stream()
                .map(callTargetMapper::toDto)
                .toList();
    }

    public CallTargetDto getTargetById(UUID id){
        return callTargetRepository.findByIdWithDetails(id)
                .map(callTargetMapper::toDto)
                .orElseThrow(()->new ResourceNotFoundException("CallTarget","id",id));
    }

    public CallTargetDto getTargetByRepAndMonth(UUID repId, Integer month, Integer year){
        CallTarget target = callTargetRepository.findByRepIdAndMonthAndYear(repId,month,year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CallTarget", "rep/month/year",
                        repId + "/" + month + "/" + year));
        return callTargetMapper.toDto(target);
    }

    @Transactional
    public CallTargetDto createTarget(CreateCallTargetRequest request){
        User rep = userRepository.findById(request.repId())
                .orElseThrow(()->new ResourceNotFoundException("User","id",request.repId()));

        User assignedBy = userRepository.findById(request.assignedById())
                .orElseThrow(()->new ResourceNotFoundException("User","id",request.assignedById()));

        if(callTargetRepository.existsByRepIdAndMonthAndYear(request.repId(),request.month(),request.year())){
            throw new IllegalArgumentException("Target already exists for this rep in "
                    + request.month() + "/" + request.year());
        }

        CallTarget target = CallTarget.builder()
                .rep(rep)
                .assignedBy(assignedBy)
                .month(request.month())
                .year(request.year())
                .targetVisits(request.targetVisits())
                .build();

        CallTarget saved = callTargetRepository.save(target);
        return callTargetMapper.toDto(callTargetRepository.findByIdWithDetails(saved.getId()).orElseThrow());
    }

    @Transactional
    public CallTargetDto updateTarget(UUID id, UpdateCallTargetRequest request){

        CallTarget target = callTargetRepository.findByIdWithDetails(id)
                .orElseThrow(()->new ResourceNotFoundException("CallTarget","id",id));

        target.setTargetVisits(request.targetVisits());
        target.setActualVisits(request.actualVisits());

        callTargetRepository.save(target);
        return callTargetMapper.toDto(callTargetRepository.findByIdWithDetails(id).orElseThrow());
    }

    // INCREMENT actualVisits — called internally when a visit is marked COMPLETED
    // This will be wired to VisitService in Phase 3
    @Transactional
    public void incrementActualVisits(UUID repId, Integer month, Integer year) {
        callTargetRepository.findByRepIdAndMonthAndYear(repId, month, year)
                .ifPresent(target -> {
                    target.setActualVisits(target.getActualVisits() + 1);
                    callTargetRepository.save(target);
                });
        // If no target exists for this period — silently do nothing
        // Not every month has a target assigned
    }
}
