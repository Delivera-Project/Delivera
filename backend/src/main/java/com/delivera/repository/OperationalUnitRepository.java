package com.delivera.repository;

import com.delivera.model.OperationalUnit;
import com.delivera.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationalUnitRepository extends JpaRepository<OperationalUnit, UUID> {

    List<OperationalUnit> findAllByCompanyId(UUID companyId);

    Optional<OperationalUnit> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndName(UUID companyId, String name);

    boolean existsByCompanyIdAndNameAndIdNot(UUID companyId, String name, UUID id);

    List<OperationalUnit> findAllByCompanyIdAndWorkersContaining(UUID companyId, Worker worker);

    @Query("SELECT u FROM OperationalUnit u JOIN FETCH u.workers WHERE u.id = :id AND u.company.id = :companyId")
    Optional<OperationalUnit> findByIdAndCompanyIdWithWorkers(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("SELECT u FROM OperationalUnit u WHERE u.company.organization.id = " +
           "(SELECT c.organization.id FROM Company c WHERE c.id = :companyId) " +
           "AND u.company.id <> :companyId")
    List<OperationalUnit> findExternalByOrganization(@Param("companyId") UUID companyId);

    long countByCompanyId(UUID companyId);
}
