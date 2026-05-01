package org.ved.crm.inventory;

public enum MovementType {
    // Stock received from manufacturer into warehouse
    INWARD,
    // Stock deducted when an invoice is generated
    SALE,
    // Stock returned by chemist or stockist
    RETURN,
    // Physician samples given to doctors — not billed but deducted from stock
    SAMPLE,
    // Manual correction by owner
    ADJUSTMENT,
    // Expired stock written off
    EXPIRY_WRITEOFF
}
