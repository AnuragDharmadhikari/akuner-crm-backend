package org.ved.crm.analytics;

import java.io.Serializable;
import java.math.BigDecimal;

public record GstLiabilityDto(

        String month,

        BigDecimal totalCgst,

        BigDecimal totalSgst,

        BigDecimal totalIgst,

        BigDecimal totalTaxLiability

)implements Serializable {
}
