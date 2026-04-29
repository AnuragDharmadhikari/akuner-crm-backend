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
        UUID stockistId,
        String stockistFirmName,
        String stockistGstin,
        String stockistState,
        LocalDate orderDate,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemDto> orderItems,
        Instant createdAt,
        Instant updatedAt
) {
}
