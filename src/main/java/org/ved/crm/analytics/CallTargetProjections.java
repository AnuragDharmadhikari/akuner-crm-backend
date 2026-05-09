package org.ved.crm.analytics;

import java.util.UUID;

public class CallTargetProjections {

    // ── Target Achievement
    public interface TargetAchievementProjection {
        UUID getRepId();
        String getRepName();
        Integer getMonth();
        Integer getYear();
        Integer getTargetVisits();
        Integer getActualVisits();
    }

}
