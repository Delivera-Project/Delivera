package com.delivera.service;

import com.delivera.config.SecurityUtils;
import com.delivera.dto.settings.*;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.CompanyHasActiveOrdersException;
import com.delivera.exception.ForbiddenException;
import com.delivera.exception.HandleConflictException;
import com.delivera.exception.UserNotFoundException;
import com.delivera.model.*;
import com.delivera.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.delivera.model.OrderStatus.IN_TRANSIT;
import static com.delivera.model.OrderStatus.PENDING;

@Service
public class SettingsService {

    @Autowired private CompanyRepository companyRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WorkerRepository workerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private LoyalUserRepository loyalUserRepository;
    @Autowired private OperationalUnitRepository operationalUnitRepository;
    @Autowired private SecurityUtils securityUtils;

    private Company currentCompany() {
        return companyRepository.findById(securityUtils.getCurrentCompanyId())
                .orElseThrow(CompanyContextException::new);
    }

    public SettingsResponse getSettings() {
        Company c = currentCompany();
        Organization o = c.getOrganization();
        return new SettingsResponse(o.getId(), o.getName(), o.getHandle(), c.getId(), c.getName(), c.getActivityType());
    }

    @Transactional
    public SettingsResponse updateOrg(OrgUpdateRequest req) {
        Company c = currentCompany();
        Organization o = c.getOrganization();
        if (!o.getHandle().equals(req.handle()) && organizationRepository.existsByHandleAndIdNot(req.handle(), o.getId())) {
            throw new HandleConflictException(req.handle(), null);
        }
        o.setName(req.name());
        o.setHandle(req.handle());
        organizationRepository.save(o);
        return getSettings();
    }

    @Transactional
    public SettingsResponse updateCompany(CompanyUpdateRequest req) {
        Company c = currentCompany();
        c.setName(req.name());
        c.setActivityType(req.activityType());
        companyRepository.save(c);
        return getSettings();
    }

    @Transactional
    public CompanySummary createCompany(CompanyCreateRequest req) {
        Company current = currentCompany();
        Organization org = current.getOrganization();

        Company newCompany = new Company();
        newCompany.setOrganization(org);
        newCompany.setName(req.name());
        newCompany.setActivityType(req.activityType());
        companyRepository.save(newCompany);

        String email = securityUtils.getCurrentEmail();
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Worker worker = new Worker();
        worker.setUser(user);
        worker.setCompany(newCompany);
        worker.setRole(WorkerRole.COMPANY_ADMIN);
        workerRepository.save(worker);

        return new CompanySummary(newCompany.getId(), newCompany.getName(), newCompany.getActivityType());
    }

    @Transactional
    public void deleteCompany(UUID companyId, boolean force) {
        Company current = currentCompany();
        Company target = companyRepository.findById(companyId).orElseThrow(() -> new ForbiddenException("Company not found"));

        if (!target.getOrganization().getId().equals(current.getOrganization().getId())) {
            throw new ForbiddenException("Company does not belong to current organization");
        }
        if (companyId.equals(current.getId())) {
            throw new ForbiddenException("Cannot delete the company you are currently logged into");
        }
        if (!force && orderRepository.existsByCompanyIdAndStatusIn(companyId, List.of(PENDING, IN_TRANSIT))) {
            throw new CompanyHasActiveOrdersException(companyId);
        }

        orderRepository.deleteAll(orderRepository.findByCompanyId(companyId));
        loyalUserRepository.deleteAll(loyalUserRepository.findByCompanyIdOrderByCreatedAtDesc(companyId));
        operationalUnitRepository.deleteAll(operationalUnitRepository.findAllByCompanyId(companyId));
        workerRepository.deleteAll(workerRepository.findByCompanyId(companyId));
        companyRepository.delete(target);
    }

    public List<CompanySummary> getMyCompanies() {
        String email = securityUtils.getCurrentEmail();
        UUID orgId = currentCompany().getOrganization().getId();
        return workerRepository.findByUserEmailAndOrgId(email, orgId).stream()
                .map(w -> new CompanySummary(w.getCompany().getId(), w.getCompany().getName(), w.getCompany().getActivityType()))
                .toList();
    }
}
