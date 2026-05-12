package org.ved.crm.analytics;

import java.io.Serializable;
import java.math.BigDecimal;

public record RevenueSummaryDto(

        String month,

        BigDecimal totalRevenue,

        long invoiceCount,

        BigDecimal averageInvoiceValue

) implements Serializable {
}
