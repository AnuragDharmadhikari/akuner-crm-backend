package org.ved.crm.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateOrderRequest(
        @NotNull @NotEmpty List<OrderItemRequest> orderItems
) {}