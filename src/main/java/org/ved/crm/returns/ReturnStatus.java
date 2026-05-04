package org.ved.crm.returns;

public enum ReturnStatus {

    // Return has been logged but not yet processed
    PENDING,
    // Return has been processed — stock adjusted, credit note raised
    PROCESSED,
    // Return was rejected — invalid claim
    REJECTED
}
