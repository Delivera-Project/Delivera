package com.delivera.repository;

import com.delivera.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByHandle(String handle);
    boolean existsByHandleAndIdNot(String handle, UUID id);
}
