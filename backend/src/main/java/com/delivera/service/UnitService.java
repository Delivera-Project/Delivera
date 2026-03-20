package com.delivera.service;

import com.delivera.dto.unit.UnitRequest;
import com.delivera.dto.unit.UnitResponse;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.UnitNameConflictException;
import com.delivera.exception.UnitNotFoundException;
import com.delivera.model.OperationalUnit;
import com.delivera.repository.CompanyRepository;
import com.delivera.repository.OperationalUnitRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UnitService {

    private final OperationalUnitRepository unitRepository;
    private final CompanyRepository companyRepository;

    public UnitService(OperationalUnitRepository unitRepository, CompanyRepository companyRepository) {
        this.unitRepository = unitRepository;
        this.companyRepository = companyRepository;
    }

    private UUID getCurrentCompanyId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof UUID companyId)) {
            throw new CompanyContextException();
        }
        return companyId;
    }

    @Transactional
    public UnitResponse create(UnitRequest request) {
        UUID companyId = getCurrentCompanyId();
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
        UUID companyId = getCurrentCompanyId();
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
        return unitRepository.findAllByCompanyId(getCurrentCompanyId()).stream()
                .map(UnitResponse::from)
                .toList();
    }
}
