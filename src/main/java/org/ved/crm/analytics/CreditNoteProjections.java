package org.ved.crm.analytics;

import java.math.BigDecimal;

public class CreditNoteProjections {

    public interface OpenCreditNoteTotalProjection{

        BigDecimal getTotalOpenValue();

        Long getOpenCount();

        BigDecimal getStockistOpenValue();

        BigDecimal getChemistOpenValue();

    }

}
