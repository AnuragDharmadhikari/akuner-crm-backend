package org.ved.crm.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID repId,
        @NotNull UUID stockistId,
        @NotNull LocalDate orderDate,
        @NotNull @NotEmpty @Valid List<OrderItemRequest> orderItems
        ) {
}
