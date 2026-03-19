package com.delivera.repository;

import com.delivera.dto.auth.OrganizationSummary;
import com.delivera.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    @Query("SELECT DISTINCT new com.delivera.dto.auth.OrganizationSummary(o.slug, o.name) FROM Worker w JOIN w.company c JOIN c.organization o JOIN w.user u WHERE u.email = :email")
    List<OrganizationSummary> findOrganizationsByUserEmail(@Param("email") String email);

    @Query("SELECT w FROM Worker w JOIN FETCH w.company c JOIN c.organization o JOIN w.user u WHERE u.email = :email AND o.slug = :slug")
    Optional<Worker> findByUserEmailAndOrganizationSlug(@Param("email") String email, @Param("slug") String slug);
}
