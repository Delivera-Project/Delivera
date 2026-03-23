package com.delivera.repository;

import com.delivera.model.Order;
import com.delivera.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query(value = "SELECT nextval('order_ref_seq')", nativeQuery = true)
    Long nextReferenceSeq();

    List<Order> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN o.destination dest LEFT JOIN dest.company dc " +
           "WHERE o.company.id = :companyId " +
           "OR (dest IS NOT NULL AND dc.id = :companyId AND o.company.id <> :companyId) " +
           "ORDER BY o.createdAt DESC")
    List<Order> findSentOrReceivedByCompanyId(@Param("companyId") UUID companyId);

    Optional<Order> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT o FROM Order o LEFT JOIN o.destination dest LEFT JOIN dest.company dc " +
           "WHERE o.id = :id AND (o.company.id = :companyId OR (dest IS NOT NULL AND dc.id = :companyId))")
    Optional<Order> findByIdForCompany(@Param("id") UUID id, @Param("companyId") UUID companyId);

    Optional<Order> findByTrackingToken(String trackingToken);

    Optional<Order> findByReference(String reference);

    List<Order> findByLoyalUserIdOrderByCreatedAtDesc(UUID loyalUserId);

    long countByLoyalUserId(UUID loyalUserId);

    boolean existsByCompanyIdAndStatusIn(UUID companyId, List<OrderStatus> statuses);

    List<Order> findByCompanyId(UUID companyId);
}
