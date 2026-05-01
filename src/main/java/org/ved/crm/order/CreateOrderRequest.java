package org.ved.crm.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        @NotNull
        UUID repId,

        // Chemist is always required — they are always the buyer
        @NotNull
        UUID chemistId,

        // Stockist is optional — null means direct sale from company to chemist
        UUID stockistId,

        // Must explicitly state how this order will be fulfilled
        @NotNull
        FulfillmentType fulfillmentType,

        @NotNull
        LocalDate orderDate,

        @NotNull
        @NotEmpty
        @Valid
        List<OrderItemRequest> orderItems
) {
}