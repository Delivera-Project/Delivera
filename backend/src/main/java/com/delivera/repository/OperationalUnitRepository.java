package com.delivera.repository;

import com.delivera.model.OperationalUnit;
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

    @Query("SELECT u FROM OperationalUnit u WHERE u.company.organization.id = " +
           "(SELECT c.organization.id FROM Company c WHERE c.id = :companyId) " +
           "AND u.company.id <> :companyId")
    List<OperationalUnit> findExternalByOrganization(@Param("companyId") UUID companyId);

    long countByCompanyId(UUID companyId);
}
