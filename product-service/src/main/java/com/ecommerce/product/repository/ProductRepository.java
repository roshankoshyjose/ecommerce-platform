package com.ecommerce.product.repository;

import com.ecommerce.product.enums.Category;
import com.ecommerce.product.enums.ProductStatus;
import com.ecommerce.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByCategory(Category category);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategoryAndStatus(Category category, ProductStatus status);
}
