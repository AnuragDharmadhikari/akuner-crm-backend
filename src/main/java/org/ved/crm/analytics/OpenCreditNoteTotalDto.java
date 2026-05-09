package org.ved.crm.analytics;

import java.math.BigDecimal;

public record OpenCreditNoteTotalDto(

        BigDecimal totalOpenValue,

        long openCount,

        BigDecimal stockistOpenValue,

        BigDecimal chemistOpenValue

) {
}
