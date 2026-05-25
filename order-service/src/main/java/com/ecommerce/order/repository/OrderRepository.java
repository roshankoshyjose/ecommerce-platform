package com.ecommerce.order.repository;

import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.model.Order;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("Select o from Order o join FETCH o.items where o.id = :id")
    Optional<Order> findByIdWithItems(@Id Long id);
}
