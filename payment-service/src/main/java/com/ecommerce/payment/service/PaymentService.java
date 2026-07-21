package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentById(Long id);

    List<PaymentResponse> getPaymentsByOrderId(Long orderId);

    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);

    PaymentResponse refundPayment(Long id);
}
