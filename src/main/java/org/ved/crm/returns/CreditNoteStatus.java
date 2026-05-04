package org.ved.crm.returns;

public enum CreditNoteStatus {
    // Credit note raised but not yet applied to any invoice
    OPEN,
    // Credit note has been applied to an invoice
    APPLIED,
    // Credit note cancelled — no longer valid
    VOID
}
