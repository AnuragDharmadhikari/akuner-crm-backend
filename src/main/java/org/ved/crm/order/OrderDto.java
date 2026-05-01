package org.ved.crm.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID repId,
        String repName,

        // Chemist fields — always present, chemist is always the buyer
        UUID chemistId,
        String chemistFirmName,
        String chemistGstin,
        String chemistState,

        // Stockist fields — nullable when fulfillmentType is DIRECT
        UUID stockistId,
        String stockistFirmName,
        String stockistGstin,
        String stockistState,

        // VIA_STOCKIST or DIRECT
        FulfillmentType fulfillmentType,

        LocalDate orderDate,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemDto> orderItems,
        Instant createdAt,
        Instant updatedAt
) {
}