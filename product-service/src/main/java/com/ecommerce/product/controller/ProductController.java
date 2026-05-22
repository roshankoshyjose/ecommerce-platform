package com.ecommerce.product.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.enums.Category;
import com.ecommerce.product.enums.ProductStatus;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {

        ProductResponse response = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product Created Successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {

        List<ProductResponse> responses = productService.getAllProducts();

        return ResponseEntity.ok(
                ApiResponse.ok("Products Fetched Successfully", responses)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id
    ) {

        ProductResponse response = productService.getProductById(id);

        return ResponseEntity.ok(
                ApiResponse.ok("Product Fetched Successfully", response)
        );
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(
            @PathVariable String sku
    ) {

        ProductResponse response = productService.getProductBySku(sku);

        return ResponseEntity.ok(
                ApiResponse.ok("Product Fetched Successfully", response)
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable Category category
    ) {

        List<ProductResponse> responses =
                productService.getProductsByCategory(category);

        return ResponseEntity.ok(
                ApiResponse.ok("Products Fetched Successfully", responses)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByStatus(
            @PathVariable ProductStatus status
    ) {

        List<ProductResponse> responses =
                productService.getProductsByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.ok("Products Fetched Successfully", responses)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {

        ProductResponse response =
                productService.updateProduct(id, request);

        return ResponseEntity.ok(
                ApiResponse.ok("Product Updated Successfully", response)
        );
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity
    ) {

        productService.updateStock(id, quantity);

        return ResponseEntity.ok(
                ApiResponse.ok("Product Stock Updated Successfully", null)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    ) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}