package com.delivera.repository;

import com.delivera.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderEventRepository extends JpaRepository<OrderEvent, UUID> {
}
