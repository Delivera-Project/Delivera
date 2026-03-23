package com.delivera.repository;

import com.delivera.model.OrderStatusConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusConfigRepository extends JpaRepository<OrderStatusConfig, String> {
    List<OrderStatusConfig> findAllByOrderBySortOrderAsc();
}
