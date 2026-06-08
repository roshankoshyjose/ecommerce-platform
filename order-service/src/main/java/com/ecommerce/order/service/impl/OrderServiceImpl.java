package com.ecommerce.order.service.impl;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.UserClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    @Override
    public OrderResponse createOrder(OrderRequest request) {

        try {
            userClient.getUserById(request.getUserId());
        } catch (Exception ex) {
            throw new ResourceNotFoundException("User", "id", request.getUserId());
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductClientResponse product = fetchProduct(itemRequest.getProductId());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            items.add(OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build());

            total = total.add(subtotal);

        }

        Order order = Order.builder()
                .userId(request.getUserId())
                .orderNumber("ORD-" + System.currentTimeMillis())
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .shippingAddress(request.getShippingAddress())
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);

        for (OrderItemRequest itemRequest : request.getItems()) {
            try {
                productClient.updateStock(itemRequest.getProductId(), -itemRequest.getQuantity());
            } catch (Exception ex) {
                log.warn("Failed to deduct stock for product {}: {}", itemRequest.getProductId(), ex.getMessage());
            }
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return toResponse(findWithItems(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = findWithItems(id);

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = findWithItems(id);

        if (order.getStatus().equals(OrderStatus.SHIPPED) || order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new BadRequestException("Order cancelled");
        }

        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            throw new BadRequestException("Order already cancelled");
        }

        order.getItems().forEach(item -> {
            try {
                productClient.updateStock(item.getProductId(), item.getQuantity());
            } catch (Exception ex) {
                log.warn("Failed to restore stock for product {}: {}", item.getProductId(), ex.getMessage());
            }
        });
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private Order findWithItems(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    private ProductClientResponse fetchProduct(Long productId) {
        try {
            com.ecommerce.common.dto.ApiResponse<ProductClientResponse> response = productClient.getProductById(productId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Product", "id", productId);
            }
            return response.getData();
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {

        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            default -> false;
        };

        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " -> " + next
            );
        }
    }
}
