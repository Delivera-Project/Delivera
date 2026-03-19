package com.delivera.service;

import com.delivera.dto.unit.UnitRequest;
import com.delivera.dto.unit.UnitResponse;
import com.delivera.exception.UnitNameConflictException;
import com.delivera.exception.UnitNotFoundException;
import com.delivera.model.OperationalUnit;
import com.delivera.repository.CompanyRepository;
import com.delivera.repository.OperationalUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UnitService {

    @Autowired
    private OperationalUnitRepository unitRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private UUID getCurrentCompanyId() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof String s && !s.isBlank()) {
            return UUID.fromString(s);
        }
        throw new AccessDeniedException("No company context in token");
    }

    @Transactional
    public UnitResponse create(UnitRequest request) {
        UUID companyId = getCurrentCompanyId();
        if (unitRepository.existsByCompanyIdAndName(companyId, request.name())) {
            throw new UnitNameConflictException();
        }
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AccessDeniedException("Company not found"));
        var unit = new OperationalUnit();
        unit.setCompany(company);
        unit.setName(request.name());
        unit.setType(request.type());
        unit.setAddress(request.address());
        unit.setLatitude(request.latitude());
        unit.setLongitude(request.longitude());
        return new UnitResponse(unitRepository.save(unit));
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
        return new UnitResponse(unitRepository.save(unit));
    }

    public List<UnitResponse> getByCompany() {
        return unitRepository.findAllByCompanyId(getCurrentCompanyId()).stream()
                .map(UnitResponse::new)
                .toList();
    }
}
