package com.ecommerce.payment.service.impl;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.payment.client.OrderClient;
import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.enums.PaymentStatus;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {

        Map<String, Object> order;
        try {
            ApiResponse<Map<String, Object>> orderResponse = orderClient.getOrder(request.getOrderId());
            order = orderResponse.getData();
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Order", "id", request.getOrderId());
        }

        if (paymentRepository.existsByOrderIdAndStatus(request.getOrderId(), PaymentStatus.SUCCESS)) {
            throw new BadRequestException("Order " + request.getOrderId() + " has already been processed");
        }

        boolean gatewaySuccess = simulateGatewayCharge();

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(gatewaySuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .transactionId(generateTransactionId())
                .failureReason(gatewaySuccess ? null : "Gateway declined the charge")
                .build();

        Payment saved = paymentRepository.save(payment);

        try {
            orderClient.updateOrderStatus(
                    request.getOrderId(),
                    gatewaySuccess ? "CONFIRMED" : "PAYMENT_FAILED"
            );
        } catch (FeignException.NotFound e) {
            log.warn("Payment {} recorded but order-service status update failed for order {}: {}",
                    saved.getId(), request.getOrderId(), e.getMessage());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only a SUCCESS payment can be refunded (current status: " + payment.getStatus() + ")");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);

        try {
            orderClient.updateOrderStatus(payment.getOrderId(), "CANCELLED");
        } catch (FeignException.NotFound e) {
            log.warn("Refund {} recorded but order-service status update failed for order {}: {}",
                    refundedPayment.getId(), payment.getOrderId(), e.getMessage());
        }
        return toResponse(refundedPayment);
    }

    private boolean simulateGatewayCharge() {
        // Fake latency + a 90% success rate, standing in for a real processor call
        return ThreadLocalRandom.current().nextInt(100) < 90;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
