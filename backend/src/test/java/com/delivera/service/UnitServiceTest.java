package com.delivera.service;

import com.delivera.security.SecurityUtils;
import com.delivera.dto.unit.UnitRequest;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.UnitNameConflictException;
import com.delivera.exception.UnitNotFoundException;
import com.delivera.model.Company;
import com.delivera.model.OperationalUnit;
import com.delivera.model.UnitType;
import com.delivera.repository.CompanyRepository;
import com.delivera.repository.OperationalUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private OperationalUnitRepository unitRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private SecurityUtils securityUtils;
    @InjectMocks
    private UnitService unitService;

    private UUID companyId;
    private Company company;
    private OperationalUnit unit;
    private UnitRequest request;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);

        unit = new OperationalUnit();
        unit.setId(UUID.randomUUID());
        unit.setName("Warehouse A");
        unit.setType(UnitType.WAREHOUSE);
        unit.setCompany(company);

        request = new UnitRequest("Warehouse A", UnitType.WAREHOUSE, "1 Main St", null, null);
    }

    @Test
    void create_success() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.existsByCompanyIdAndName(companyId, "Warehouse A")).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(unitRepository.save(any())).thenReturn(unit);

        assertThat(unitService.create(request)).isNotNull();
        verify(unitRepository).save(any());
    }

    @Test
    void create_nameDuplicate_throws() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.existsByCompanyIdAndName(companyId, "Warehouse A")).thenReturn(true);

        assertThatThrownBy(() -> unitService.create(request))
                .isInstanceOf(UnitNameConflictException.class);
    }

    @Test
    void update_success() {
        UUID unitId = unit.getId();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.of(unit));
        when(unitRepository.existsByCompanyIdAndNameAndIdNot(companyId, "Warehouse A", unitId)).thenReturn(false);
        when(unitRepository.save(unit)).thenReturn(unit);

        assertThat(unitService.update(unitId, request)).isNotNull();
    }

    @Test
    void delete_success() {
        UUID unitId = unit.getId();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.of(unit));

        unitService.delete(unitId);
        verify(unitRepository).delete(unit);
    }

}
