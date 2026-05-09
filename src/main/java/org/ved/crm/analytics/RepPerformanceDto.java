package org.ved.crm.analytics;

import java.math.BigDecimal;
import java.util.UUID;

public record RepPerformanceDto(

        UUID repId,

        String repName,

        long totalVisits,

        long completedVisits,

        long totalOrders,

        BigDecimal totalRevenue,

        Integer targetVisits,

        BigDecimal achievementPct

) {
}
