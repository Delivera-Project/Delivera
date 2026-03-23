package com.delivera.repository;

import com.delivera.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    @Query("SELECT w FROM Worker w JOIN FETCH w.company c JOIN c.organization o JOIN w.user u WHERE u.email = :email AND o.handle = :handle ORDER BY w.createdAt ASC")
    List<Worker> findByUserEmailAndOrganizationHandle(@Param("email") String email, @Param("handle") String handle);

    boolean existsByUserEmail(String email);

    @Query("SELECT w FROM Worker w JOIN FETCH w.company c JOIN c.organization o JOIN w.user u WHERE u.email = :email AND c.id = :companyId")
    Optional<Worker> findByUserEmailAndCompanyId(@Param("email") String email, @Param("companyId") UUID companyId);

    @Query("SELECT w FROM Worker w JOIN FETCH w.company c JOIN c.organization o JOIN w.user u WHERE u.email = :email AND o.id = :orgId")
    List<Worker> findByUserEmailAndOrgId(@Param("email") String email, @Param("orgId") UUID orgId);

    List<Worker> findByCompanyId(UUID companyId);

    @Query("SELECT w FROM Worker w JOIN FETCH w.company c JOIN c.organization o JOIN w.user u WHERE u.email = :email ORDER BY w.createdAt ASC")
    List<Worker> findByUserEmailOrderByCreatedAtAsc(@Param("email") String email);
}
