package org.ved.crm.analytics;

import java.math.BigDecimal;

public record GstLiabilityDto(

        String month,

        BigDecimal totalCgst,

        BigDecimal totalSgst,

        BigDecimal totalIgst,

        BigDecimal totalTaxLiability

) {
}
