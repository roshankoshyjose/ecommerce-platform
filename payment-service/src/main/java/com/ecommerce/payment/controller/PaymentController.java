package com.ecommerce.payment.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.enums.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Payment processed", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment fetched", paymentService.getPaymentById(id)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Payments fetched", paymentService.getPaymentsByOrderId(orderId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Payments fetched", paymentService.getPaymentsByStatus(status)));
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment refunded", paymentService.refundPayment(id)));
    }
}