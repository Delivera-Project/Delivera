package com.delivera.repository;

import com.delivera.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    List<ApiKey> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    Optional<ApiKey> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<ApiKey> findByKeyHash(String keyHash);
}
