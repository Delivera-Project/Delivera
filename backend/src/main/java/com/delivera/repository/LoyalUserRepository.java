package com.delivera.repository;

import com.delivera.model.LoyalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoyalUserRepository extends JpaRepository<LoyalUser, UUID> {

    List<LoyalUser> findByCompaniesIdOrderByCreatedAtDesc(UUID companyId);

    Optional<LoyalUser> findByCompaniesIdAndEmail(UUID companyId, String email);

    Optional<LoyalUser> findByIdAndCompaniesId(UUID id, UUID companyId);

    @Query("SELECT lu FROM LoyalUser lu WHERE lu.email = :email")
    List<LoyalUser> findByEmail(String email);

    long countByCompaniesId(UUID companyId);
}
