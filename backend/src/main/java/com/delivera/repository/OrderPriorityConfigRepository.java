package com.delivera.repository;

import com.delivera.model.OrderPriorityConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderPriorityConfigRepository extends JpaRepository<OrderPriorityConfig, String> {
    List<OrderPriorityConfig> findAllByOrderBySortOrderAsc();
}
