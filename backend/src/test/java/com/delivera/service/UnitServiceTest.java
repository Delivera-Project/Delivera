package com.delivera.service;

import com.delivera.config.SecurityUtils;
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

    @Mock private OperationalUnitRepository unitRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private SecurityUtils securityUtils;
    @InjectMocks private UnitService unitService;

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
    void create_companyNotFound_throws() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.existsByCompanyIdAndName(companyId, "Warehouse A")).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.create(request))
                .isInstanceOf(CompanyContextException.class);
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
    void update_notFound_throws() {
        UUID unitId = UUID.randomUUID();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.update(unitId, request))
                .isInstanceOf(UnitNotFoundException.class);
    }

    @Test
    void update_nameTakenByOtherUnit_throws() {
        UUID unitId = unit.getId();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.of(unit));
        when(unitRepository.existsByCompanyIdAndNameAndIdNot(companyId, "Warehouse A", unitId)).thenReturn(true);

        assertThatThrownBy(() -> unitService.update(unitId, request))
                .isInstanceOf(UnitNameConflictException.class);
    }

    @Test
    void getByCompany_returnsMappedList() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findAllByCompanyId(companyId)).thenReturn(List.of(unit));

        assertThat(unitService.getByCompany()).hasSize(1);
    }

    @Test
    void getDetail_found() {
        UUID unitId = unit.getId();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.of(unit));

        assertThat(unitService.getDetail(unitId)).isNotNull();
    }

    @Test
    void getDetail_notFound_throws() {
        UUID unitId = UUID.randomUUID();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.getDetail(unitId))
                .isInstanceOf(UnitNotFoundException.class);
    }

    @Test
    void delete_success() {
        UUID unitId = unit.getId();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.of(unit));

        unitService.delete(unitId);
        verify(unitRepository).delete(unit);
    }

    @Test
    void delete_notFound_throws() {
        UUID unitId = UUID.randomUUID();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(unitId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.delete(unitId))
                .isInstanceOf(UnitNotFoundException.class);
    }
}
