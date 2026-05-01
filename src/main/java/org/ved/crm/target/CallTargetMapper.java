package org.ved.crm.target;

import org.springframework.stereotype.Component;

@Component
public class CallTargetMapper {

    public CallTargetDto toDto(CallTarget callTarget){
        // Calculate achievement percentage
        // If targetVisits is 0 avoid division by zero — return 0.0
        double achievementPct = callTarget.getTargetVisits() > 0
                ? ((double) callTarget.getActualVisits()
                / callTarget.getTargetVisits()) * 100.0
                : 0.0;

        // Round to 2 decimal places
        achievementPct = Math.round(achievementPct * 100.0) / 100.0;

        return new CallTargetDto(
                callTarget.getId(),
                callTarget.getRep().getId(),
                callTarget.getRep().getFullName(),
                callTarget.getAssignedBy().getId(),
                callTarget.getAssignedBy().getFullName(),
                callTarget.getMonth(),
                callTarget.getYear(),
                callTarget.getTargetVisits(),
                callTarget.getActualVisits(),
                achievementPct,
                callTarget.getCreatedAt(),
                callTarget.getUpdatedAt()
        );
    }
}
