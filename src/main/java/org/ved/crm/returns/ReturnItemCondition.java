package org.ved.crm.returns;

public enum ReturnItemCondition {
    // Stock is in good condition — can go back to inventory for resale
    SALEABLE,
    // Stock is physically damaged — must be written off
    DAMAGED,
    // Stock has expired — must be written off
    EXPIRED
}
