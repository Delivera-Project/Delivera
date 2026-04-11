package com.delivera.service;

import com.delivera.dto.settings.SubscriptionUsageResponse;
import com.delivera.dto.settings.SubscriptionUsageResponse.ResourceUsage;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.SubscriptionLimitException;
import com.delivera.model.Company;
import com.delivera.model.SubscriptionPlan;
import com.delivera.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final CompanyRepository companyRepository;
    private final OperationalUnitRepository unitRepository;
    private final WorkerRepository workerRepository;
    private final OrderRepository orderRepository;
    private final LoyalUserRepository loyalUserRepository;

    public SubscriptionService(CompanyRepository companyRepository,
                               OperationalUnitRepository unitRepository,
                               WorkerRepository workerRepository,
                               OrderRepository orderRepository,
                               LoyalUserRepository loyalUserRepository) {
        this.companyRepository = companyRepository;
        this.unitRepository = unitRepository;
        this.workerRepository = workerRepository;
        this.orderRepository = orderRepository;
        this.loyalUserRepository = loyalUserRepository;
    }

    public void checkUnitLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(unitRepository.countByCompanyId(companyId), plan.getMaxUnits())) {
            throw new SubscriptionLimitException("units");
        }
    }

    public void checkWorkerLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(workerRepository.countByCompanyId(companyId), plan.getMaxWorkers())) {
            throw new SubscriptionLimitException("workers");
        }
    }

    public void checkOrderLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(orderRepository.countByCompanyIdAndCreatedAtAfter(companyId, startOfMonth()), plan.getMaxOrdersPerMonth())) {
            throw new SubscriptionLimitException("orders");
        }
    }

    public void checkLoyalUserLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(loyalUserRepository.countByCompaniesId(companyId), plan.getMaxLoyalUsers())) {
            throw new SubscriptionLimitException("loyal_users");
        }
    }

    public void checkCompanyLimit(UUID companyId) {
        Company company = getCompany(companyId);
        SubscriptionPlan plan = company.getPlan();
        if (!plan.allows(companyRepository.countByOrganizationId(company.getOrganization().getId()), plan.getMaxCompanies())) {
            throw new SubscriptionLimitException("companies");
        }
    }

    public SubscriptionUsageResponse getUsage(UUID companyId) {
        Company company = getCompany(companyId);
        SubscriptionPlan plan = company.getPlan();
        Instant som = startOfMonth();
        return new SubscriptionUsageResponse(
                plan.getCode(),
                plan.getName(),
                new ResourceUsage(unitRepository.countByCompanyId(companyId), plan.getMaxUnits()),
                new ResourceUsage(workerRepository.countByCompanyId(companyId), plan.getMaxWorkers()),
                new ResourceUsage(orderRepository.countByCompanyIdAndCreatedAtAfter(companyId, som), plan.getMaxOrdersPerMonth()),
                new ResourceUsage(loyalUserRepository.countByCompaniesId(companyId), plan.getMaxLoyalUsers()),
                new ResourceUsage(companyRepository.countByOrganizationId(company.getOrganization().getId()), plan.getMaxCompanies())
        );
    }

    private SubscriptionPlan getPlan(UUID companyId) {
        return getCompany(companyId).getPlan();
    }

    private Company getCompany(UUID companyId) {
        return companyRepository.findById(companyId).orElseThrow(CompanyContextException::new);
    }

    private Instant startOfMonth() {
        return java.time.LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
    }
}
