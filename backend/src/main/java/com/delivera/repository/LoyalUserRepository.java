package com.delivera.repository;

import com.delivera.model.LoyalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoyalUserRepository extends JpaRepository<LoyalUser, UUID> {

    List<LoyalUser> findByCompaniesIdOrderByCreatedAtDesc(UUID companyId);

    @Query("SELECT lu, (SELECT COUNT(o) FROM Order o WHERE o.loyalUser = lu) " +
           "FROM LoyalUser lu LEFT JOIN FETCH lu.user JOIN lu.companies c " +
           "WHERE c.id = :companyId ORDER BY lu.createdAt DESC")
    List<Object[]> findWithOrderCountByCompanyId(@Param("companyId") UUID companyId);

    Optional<LoyalUser> findByCompaniesIdAndEmail(UUID companyId, String email);

    Optional<LoyalUser> findByIdAndCompaniesId(UUID id, UUID companyId);

    @Query("SELECT lu FROM LoyalUser lu WHERE lu.email = :email")
    List<LoyalUser> findByEmail(String email);

    long countByCompaniesId(UUID companyId);

    long countByCompaniesIdAndCreatedAtAfter(UUID companyId, Instant createdAtAfter);
}
