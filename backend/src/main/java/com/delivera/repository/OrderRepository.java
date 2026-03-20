package com.delivera.repository;

import com.delivera.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query(value = "SELECT nextval('order_ref_seq')", nativeQuery = true)
    Long nextReferenceSeq();

    List<Order> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
