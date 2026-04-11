package com.delivera.service;

import com.delivera.dto.settings.CompanySummary;
import com.delivera.dto.unit.B2BUnitResponse;
import com.delivera.dto.unit.UnitRequest;
import com.delivera.dto.unit.UnitResponse;
import com.delivera.security.SecurityUtils;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.UnitNameConflictException;
import com.delivera.exception.UnitNotFoundException;
import com.delivera.model.Company;
import com.delivera.model.OperationalUnit;
import com.delivera.repository.CompanyRepository;
import com.delivera.repository.OperationalUnitRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UnitService {

    private final OperationalUnitRepository unitRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtils securityUtils;
    private final SubscriptionService subscriptionService;

    public UnitService(OperationalUnitRepository unitRepository,
                       CompanyRepository companyRepository,
                       SecurityUtils securityUtils,
                       SubscriptionService subscriptionService) {
        this.unitRepository = unitRepository;
        this.companyRepository = companyRepository;
        this.securityUtils = securityUtils;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public UnitResponse create(UnitRequest request) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        subscriptionService.checkUnitLimit(companyId);
        if (unitRepository.existsByCompanyIdAndName(companyId, request.name())) {
            throw new UnitNameConflictException();
        }
        var company = companyRepository.findById(companyId)
                .orElseThrow(CompanyContextException::new);
        var unit = new OperationalUnit();
        unit.setCompany(company);
        unit.setName(request.name());
        unit.setType(request.type());
        unit.setAddress(request.address());
        unit.setLatitude(request.latitude());
        unit.setLongitude(request.longitude());
        try {
            return UnitResponse.from(unitRepository.save(unit));
        } catch (DataIntegrityViolationException e) {
            throw new UnitNameConflictException();
        }
    }

    @Transactional
    public UnitResponse update(UUID unitId, UnitRequest request) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        var unit = unitRepository.findByIdAndCompanyId(unitId, companyId)
                .orElseThrow(() -> new UnitNotFoundException(unitId));
        if (unitRepository.existsByCompanyIdAndNameAndIdNot(companyId, request.name(), unitId)) {
            throw new UnitNameConflictException();
        }
        unit.setName(request.name());
        unit.setType(request.type());
        unit.setAddress(request.address());
        unit.setLatitude(request.latitude());
        unit.setLongitude(request.longitude());
        try {
            return UnitResponse.from(unitRepository.save(unit));
        } catch (DataIntegrityViolationException e) {
            throw new UnitNameConflictException();
        }
    }

    public List<UnitResponse> getByCompany() {
        return unitRepository.findAllByCompanyId(securityUtils.getCurrentCompanyId()).stream()
                .map(UnitResponse::from)
                .toList();
    }

    public List<B2BUnitResponse> getExternalUnits() {
        return unitRepository.findExternalByOrganization(securityUtils.getCurrentCompanyId()).stream()
                .map(B2BUnitResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanySummary> getExternalCompanies() {
        UUID companyId = securityUtils.getCurrentCompanyId();
        Company company = companyRepository.findById(companyId).orElseThrow(CompanyContextException::new);
        return companyRepository.findByOrganizationId(company.getOrganization().getId())
                .stream()
                .filter(c -> !c.getId().equals(companyId))
                .map(c -> new CompanySummary(c.getId(), c.getName(), c.getActivityType().getCode()))
                .toList();
    }

    public UnitResponse getDetail(UUID id) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        return UnitResponse.from(unitRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new UnitNotFoundException(id)));
    }

    @Transactional
    public void delete(UUID id) {
        UUID companyId = securityUtils.getCurrentCompanyId();
        var unit = unitRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new UnitNotFoundException(id));
        unitRepository.delete(unit);
    }
}
