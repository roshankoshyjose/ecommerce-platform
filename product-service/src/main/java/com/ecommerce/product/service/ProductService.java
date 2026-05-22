package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.enums.Category;
import com.ecommerce.product.enums.ProductStatus;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    List<ProductResponse> getProductsByCategory(Category category);

    List<ProductResponse> getProductsByStatus(ProductStatus status);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void updateStock(Long id, int quantity);

    void deleteProduct(Long id);
}
