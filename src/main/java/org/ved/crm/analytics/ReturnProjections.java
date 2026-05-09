package org.ved.crm.analytics;

import java.math.BigDecimal;

public class ReturnProjections {

    // ── Returns Summary
    public interface ReturnsSummaryProjection {
        String getMonth();
        Long getTotalReturnCount();
        Long getProcessedReturnCount();
        Long getRejectedReturnCount();
        BigDecimal getTotalReturnValue();
        BigDecimal getChemistReturnValue();
        BigDecimal getStockistReturnValue();
    }

}
