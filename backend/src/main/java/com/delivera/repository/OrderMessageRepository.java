package com.delivera.repository;

import com.delivera.model.OrderMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderMessageRepository extends JpaRepository<OrderMessage, UUID> {
    List<OrderMessage> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
