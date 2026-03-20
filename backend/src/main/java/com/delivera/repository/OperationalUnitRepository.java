package com.delivera.repository;

import com.delivera.model.OperationalUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationalUnitRepository extends JpaRepository<OperationalUnit, UUID> {

    List<OperationalUnit> findAllByCompanyId(UUID companyId);

    Optional<OperationalUnit> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndName(UUID companyId, String name);

    boolean existsByCompanyIdAndNameAndIdNot(UUID companyId, String name, UUID id);
}
