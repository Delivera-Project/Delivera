package com.delivera.repository;

import com.delivera.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByHandle(String handle);
    boolean existsByHandleAndIdNot(String handle, UUID id);

    @Query("SELECT o.id, o.name, o.handle, o.createdAt, " +
           "(SELECT COUNT(c) FROM Company c WHERE c.organization = o), " +
           "(SELECT COUNT(w) FROM Worker w WHERE w.company.organization = o), " +
           "(SELECT COUNT(ord) FROM Order ord WHERE ord.company.organization = o) " +
           "FROM Organization o ORDER BY o.createdAt DESC")
    List<Object[]> findAllSummaries();
}
