package com.ecommerce.payment.client;

import com.ecommerce.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "order-service", url = "${services.order.url}")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    ApiResponse<Map<String, Object>> getOrder(@PathVariable("id") Long id);

    @PatchMapping("/api/orders/{id}/status")
    ApiResponse<Map<String, Object>> updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status);
}
