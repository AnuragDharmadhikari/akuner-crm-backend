package org.ved.crm.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.chemist.Chemist;
import org.ved.crm.chemist.ChemistRepository;
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
    private final UserRepository userRepository;
    private final ChemistRepository chemistRepository;
    private final StockistRepository stockistRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllWithDetails()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public OrderDto getOrderById(UUID id) {
        return orderRepository.findByIdWithDetails(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    public List<OrderDto> getOrdersByChemist(UUID chemistId) {
        return orderRepository.findByChemistIdWithDetails(chemistId)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public List<OrderDto> getOrdersByStockist(UUID stockistId) {
        return orderRepository.findByStockistIdWithDetails(stockistId)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public List<OrderDto> getOrdersByRep(UUID repId) {
        return orderRepository.findByRepIdWithDetails(repId)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {

        // Validate rep
        User rep = userRepository.findById(request.repId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.repId()));

        // Validate chemist — always required
        Chemist chemist = chemistRepository.findByIdWithDetails(request.chemistId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chemist", "id", request.chemistId()));

        // Validate stockist only if VIA_STOCKIST
        Stockist stockist = null;
        if (request.fulfillmentType() == FulfillmentType.VIA_STOCKIST) {
            if (request.stockistId() == null) {
                throw new IllegalArgumentException(
                        "Stockist ID is required for VIA_STOCKIST fulfillment");
            }
            stockist = stockistRepository.findByIdWithDetails(request.stockistId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Stockist", "id", request.stockistId()));
        }

        // Build order
        Order order = Order.builder()
                .rep(rep)
                .chemist(chemist)
                .stockist(stockist)
                .fulfillmentType(request.fulfillmentType())
                .orderDate(request.orderDate())
                .totalAmount(BigDecimal.ZERO)
                .build();

        Order saved = orderRepository.save(order);

        // Build items using helper
        List<OrderItem> items = buildOrderItems(request.orderItems(), saved);
        saved.getOrderItems().addAll(items);

        BigDecimal total = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        saved.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        orderRepository.save(saved);

        return orderMapper.toDto(
                orderRepository.findByIdWithDetails(saved.getId()).orElseThrow());
    }

    @Transactional
    public OrderDto updateOrder(UUID id, UpdateOrderRequest request) {

        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be modified");
        }

        // Clear existing — orphanRemoval deletes from DB
        order.getOrderItems().clear();

        List<OrderItem> newItems = buildOrderItems(request.orderItems(), order);
        order.getOrderItems().addAll(newItems);

        BigDecimal total = newItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        orderRepository.save(order);

        return orderMapper.toDto(
                orderRepository.findByIdWithDetails(id).orElseThrow());
    }

    @Transactional
    public OrderDto updateOrderStatus(UUID id, OrderStatus status) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        order.setStatus(status);
        orderRepository.save(order);
        return orderMapper.toDto(
                orderRepository.findByIdWithDetails(id).orElseThrow());
    }

    // Helper — builds OrderItem list from request items
    private List<OrderItem> buildOrderItems(
            List<OrderItemRequest> requests, Order order) {
        List<OrderItem> result = new ArrayList<>();
        for (OrderItemRequest req : requests) {
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", req.productId()));

            BigDecimal unitPrice = product.getDealerPrice();
            BigDecimal discountPct = req.discountPct() != null
                    ? req.discountPct()
                    : BigDecimal.ZERO;

            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(req.quantity()))
                    .multiply(BigDecimal.ONE.subtract(
                            discountPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);

            result.add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(req.quantity())
                    .unitPrice(unitPrice)
                    .discountPct(discountPct)
                    .lineTotal(lineTotal)
                    .build());
        }
        return result;
    }
}