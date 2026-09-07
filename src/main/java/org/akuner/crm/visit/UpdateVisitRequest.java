package org.akuner.crm.visit;

import java.util.List;

public record UpdateVisitRequest(
        VisitStatus status,
        String notes,
        List<VisitProductRequest> products
) {}