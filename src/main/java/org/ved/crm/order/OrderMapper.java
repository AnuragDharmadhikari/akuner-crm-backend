package org.ved.crm.order;

import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getRep().getId(),
                order.getRep().getFullName(),

                // Chemist — always present
                order.getChemist().getId(),
                order.getChemist().getFirmName(),
                order.getChemist().getGstin(),
                order.getChemist().getState(),

                // Stockist — null safe because stockist is nullable for DIRECT orders
                order.getStockist() != null ? order.getStockist().getId() : null,
                order.getStockist() != null ? order.getStockist().getFirmName() : null,
                order.getStockist() != null ? order.getStockist().getGstin() : null,
                order.getStockist() != null ? order.getStockist().getState() : null,

                order.getFulfillmentType(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getOrderItems().stream()
                        .map(this::toOrderItemDto)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemDto toOrderItemDto(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getHsnCode(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountPct(),
                item.getLineTotal()
        );
    }
}