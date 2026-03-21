package com.delivera.repository;

import com.delivera.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query(value = "SELECT nextval('order_ref_seq')", nativeQuery = true)
    Long nextReferenceSeq();

    List<Order> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    Optional<Order> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<Order> findByTrackingToken(String trackingToken);

    Optional<Order> findByReference(String reference);

    List<Order> findByLoyalUserIdOrderByCreatedAtDesc(UUID loyalUserId);
}
