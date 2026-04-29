package org.ved.crm.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.common.exception.ResourceNotFoundException;
import org.ved.crm.product.Product;
import org.ved.crm.product.ProductRepository;
import org.ved.crm.stockist.Stockist;
import org.ved.crm.stockist.StockistRepository;
import org.ved.crm.user.User;
import org.ved.crm.user.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final StockistRepository stockistRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public List<OrderDto> getAllOrders(){
        return orderRepository.findAllWithDetails()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public OrderDto getOrderById(UUID id){
        return orderRepository.findByIdWithDetails(id)
                .map(orderMapper::toDto)
                .orElseThrow(()->new ResourceNotFoundException("Order","id",id));
    }

    public List<OrderDto> getOrdersByStockist(UUID stockistId){
        return orderRepository.findByStockistIdWithDetails(stockistId)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public List<OrderDto> getOrdersByRep(UUID repId){
        return orderRepository.findByRepIdWithDetails(repId)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request){
        User rep = userRepository.findById(request.repId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.repId()));

        Stockist stockist = stockistRepository.findById(request.stockistId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stockist", "id", request.stockistId()));

        Order order = Order.builder()
                .rep(rep)
                .stockist(stockist)
                .orderDate(request.orderDate())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        Order saved = orderRepository.save(order);

        List<OrderItem> items = buildOrderItems(
                request.orderItems(),saved);
        orderItemRepository.saveAll(items);

        BigDecimal total = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        saved.setOrderItems(items);
        saved.setTotalAmount(total);
        orderRepository.save(saved);

        return orderMapper.toDto(orderRepository.findByIdWithDetails(saved.getId()).orElseThrow());

    }

    @Transactional
    public OrderDto updateOrderStatus(UUID id, OrderStatus status) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", id));
        order.setStatus(status);
        orderRepository.save(order);
        return orderMapper.toDto(
                orderRepository.findByIdWithDetails(id).orElseThrow());
    }

    @Transactional
    public OrderDto updateOrder(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING orders can be modified");
        }

        order.getOrderItems().clear();
        List<OrderItem> newItems = buildOrderItems(
                request.orderItems(), order);
        order.getOrderItems().addAll(newItems);

        BigDecimal total = newItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        orderRepository.save(order);

        return orderMapper.toDto(
                orderRepository.findByIdWithDetails(id).orElseThrow());
    }

    private List<OrderItem> buildOrderItems(
            List<OrderItemRequest> requests, Order order) {
        List<OrderItem> result = new ArrayList<>();
        for (OrderItemRequest req : requests) {
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", req.productId()));

            BigDecimal unitPrice = product.getDealerPrice();
            BigDecimal quantity = BigDecimal.valueOf(req.quantity());
            BigDecimal discount = req.discountPct() != null
                    ? req.discountPct()
                    : BigDecimal.ZERO;

            BigDecimal lineTotal = unitPrice
                    .multiply(quantity)
                    .multiply(BigDecimal.ONE.subtract(
                            discount.divide(
                                    BigDecimal.valueOf(100),
                                    4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);

            result.add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(req.quantity())
                    .unitPrice(unitPrice)
                    .discountPct(discount)
                    .lineTotal(lineTotal)
                    .build());
        }
        return result;
    }
}
