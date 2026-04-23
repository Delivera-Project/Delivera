package com.delivera.repository;

import com.delivera.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByOrganizationId(UUID organizationId);
    List<Company> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    long countByOrganizationId(UUID organizationId);
}
