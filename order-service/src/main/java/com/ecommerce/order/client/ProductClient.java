package com.ecommerce.order.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ApiResponse<ProductClientResponse> getProductById(@PathVariable Long id);

    @PatchMapping("/api/products/{id}/stock")
    void updateStock(@PathVariable Long id, @RequestParam int quantity);
}
