package org.ved.crm.order;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order){
        List<OrderItemDto> itemDtos = order.getOrderItems()
                .stream()
                .map(this::toItemDto)
                .toList();

        return new OrderDto(
                order.getId(),
                order.getRep().getId(),
                order.getRep().getFullName(),
                order.getStockist().getId(),
                order.getStockist().getFirmName(),
                order.getStockist().getGstin(),
                order.getStockist().getState(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                itemDtos,
                order.getCreatedAt(),
                order.getUpdatedAt()

        );
    }

    private OrderItemDto toItemDto(OrderItem item){
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
