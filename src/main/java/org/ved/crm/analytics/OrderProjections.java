package org.ved.crm.analytics;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderProjections {

    public interface RepPerformanceProjection{

        UUID getRepId();
        String getRepName();
        Long getTotalVisits();
        Long getCompletedVisits();
        Long getTotalOrders();
        BigDecimal getTotalRevenue();
        Integer getTargetVisits();
        Integer getActualVisits();
    }

}
