package org.ved.crm.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class BatchProjections {

    // ── Inventory Value

    public interface InventoryValueProjection{
        UUID getProductId();
        String getProductName();
        String getHsnCode();
        BigDecimal getDealerPrice();
        Long getTotalCurrentUnits();
        BigDecimal getTotalInventoryValue();
    }

    public interface NearExpiryValueProjection{
        UUID getBatchId();
        String getBatchNumber();
        UUID getProductId();
        String getProductName();
        LocalDate getExpiryDate();
        Long getDaysUntilExpiry();
        Long getCurrentQuantity();
        BigDecimal getDealerPrice();
        BigDecimal getValueAtRisk();
    }

}
