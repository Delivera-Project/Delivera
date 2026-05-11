package com.delivera.service;

import com.delivera.dto.settings.SubscriptionUsageResponse;
import com.delivera.dto.settings.SubscriptionUsageResponse.ResourceUsage;
import com.delivera.exception.CompanyContextException;
import com.delivera.exception.SubscriptionLimitException;
import com.delivera.model.Company;
import com.delivera.model.LoyalUser;
import com.delivera.model.OperationalUnit;
import com.delivera.model.SubscriptionPlan;
import com.delivera.model.Worker;
import com.delivera.model.WorkerRole;
import com.delivera.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final CompanyRepository companyRepository;
    private final OperationalUnitRepository unitRepository;
    private final WorkerRepository workerRepository;
    private final OrderRepository orderRepository;
    private final LoyalUserRepository loyalUserRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionService(CompanyRepository companyRepository,
                               OperationalUnitRepository unitRepository,
                               WorkerRepository workerRepository,
                               OrderRepository orderRepository,
                               LoyalUserRepository loyalUserRepository,
                               SubscriptionPlanRepository subscriptionPlanRepository) {
        this.companyRepository = companyRepository;
        this.unitRepository = unitRepository;
        this.workerRepository = workerRepository;
        this.orderRepository = orderRepository;
        this.loyalUserRepository = loyalUserRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    @Transactional(readOnly = true)
    public void checkUnitLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(unitRepository.countByCompanyId(companyId), plan.getMaxUnits())) {
            throw new SubscriptionLimitException("units");
        }
    }

    @Transactional(readOnly = true)
    public void checkWorkerLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(workerRepository.countByCompanyId(companyId), plan.getMaxWorkers())) {
            throw new SubscriptionLimitException("workers");
        }
    }

    @Transactional(readOnly = true)
    public void checkOrderLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(orderRepository.countByCompanyIdAndCreatedAtAfter(companyId, startOfMonth()), plan.getMaxOrdersPerMonth())) {
            throw new SubscriptionLimitException("orders");
        }
    }

    @Transactional(readOnly = true)
    public void checkLoyalUserLimit(UUID companyId) {
        SubscriptionPlan plan = getPlan(companyId);
        if (!plan.allows(loyalUserRepository.countByCompaniesId(companyId), plan.getMaxLoyalUsers())) {
            throw new SubscriptionLimitException("loyal_users");
        }
    }

    @Transactional(readOnly = true)
    public void checkCompanyLimit(UUID companyId) {
        Company company = getCompany(companyId);
        SubscriptionPlan plan = company.getPlan();
        if (!plan.allows(companyRepository.countByOrganizationId(company.getOrganization().getId()), plan.getMaxCompanies())) {
            throw new SubscriptionLimitException("companies");
        }
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public SubscriptionUsageResponse changePlan(UUID companyId, String planCode, boolean force) {
        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(planCode)
                .orElseThrow(CompanyContextException::new);
        Company company = getCompany(companyId);
        Instant som = startOfMonth();
        if (force) {
            deleteExcessResources(companyId, newPlan);
        } else {
            if (newPlan.getMaxUnits() != -1 && unitRepository.countByCompanyId(companyId) > newPlan.getMaxUnits())
                throw new SubscriptionLimitException("units");
            if (newPlan.getMaxWorkers() != -1 && workerRepository.countByCompanyId(companyId) > newPlan.getMaxWorkers())
                throw new SubscriptionLimitException("workers");
            if (newPlan.getMaxOrdersPerMonth() != -1 && orderRepository.countByCompanyIdAndCreatedAtAfter(companyId, som) > newPlan.getMaxOrdersPerMonth())
                throw new SubscriptionLimitException("orders");
            if (newPlan.getMaxLoyalUsers() != -1 && loyalUserRepository.countByCompaniesId(companyId) > newPlan.getMaxLoyalUsers())
                throw new SubscriptionLimitException("loyal_users");
            if (newPlan.getMaxCompanies() != -1 && companyRepository.countByOrganizationId(company.getOrganization().getId()) > newPlan.getMaxCompanies())
                throw new SubscriptionLimitException("companies");
        }
        company.setPlan(newPlan);
        companyRepository.save(company);
        return getUsage(companyId);
    }

    private void deleteExcessResources(UUID companyId, SubscriptionPlan newPlan) {
        // Workers: delete newest non-admin first, then admins keeping at least one
        if (newPlan.getMaxWorkers() != -1) {
            List<Worker> workers = workerRepository.findByCompanyIdOrderByCreatedAtAsc(companyId);
            long excess = workers.size() - newPlan.getMaxWorkers();
            if (excess > 0) {
                List<Worker> reversed = new ArrayList<>(workers);
                Collections.reverse(reversed);
                List<Worker> toDelete = new ArrayList<>();
                long adminCount = workers.stream().filter(w -> w.getRole() == WorkerRole.COMPANY_ADMIN).count();
                for (Worker w : reversed) {
                    if (excess <= 0) break;
                    if (w.getRole() != WorkerRole.COMPANY_ADMIN) {
                        toDelete.add(w); excess--;
                    } else if (adminCount > 1) {
                        toDelete.add(w); excess--; adminCount--;
                    }
                }
                workerRepository.deleteAll(toDelete);
            }
        }
        // LoyalUsers: remove company association from newest ones
        if (newPlan.getMaxLoyalUsers() != -1) {
            List<LoyalUser> loyalUsers = loyalUserRepository.findByCompaniesIdOrderByCreatedAtDesc(companyId);
            long excess = loyalUsers.size() - newPlan.getMaxLoyalUsers();
            for (int i = 0; i < excess && i < loyalUsers.size(); i++) {
                LoyalUser lu = loyalUsers.get(i);
                lu.getCompanies().removeIf(c -> c.getId().equals(companyId));
                loyalUserRepository.save(lu);
            }
        }
        // Units: delete newest ones with no orders (units with orders cannot be deleted safely)
        if (newPlan.getMaxUnits() != -1) {
            long excess = unitRepository.countByCompanyId(companyId) - newPlan.getMaxUnits();
            if (excess > 0) {
                List<OperationalUnit> deletable = unitRepository.findByCompanyIdWithNoOrdersOrderByCreatedAtDesc(companyId);
                for (int i = 0; i < excess && i < deletable.size(); i++) {
                    unitRepository.delete(deletable.get(i));
                }
            }
        }
        // Companies: delete newest companies in the org (never the current company)
        if (newPlan.getMaxCompanies() != -1) {
            Company current = companyRepository.findById(companyId).orElseThrow();
            UUID orgId = current.getOrganization().getId();
            List<Company> companies = companyRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId);
            long excess = companies.size() - newPlan.getMaxCompanies();
            for (Company c : companies) {
                if (excess <= 0) break;
                if (c.getId().equals(companyId)) continue;
                orderRepository.deleteEventsByCompanyId(c.getId());
                orderRepository.deleteByCompanyId(c.getId());
                loyalUserRepository.deleteAll(loyalUserRepository.findByCompaniesIdOrderByCreatedAtDesc(c.getId()));
                unitRepository.deleteByCompanyId(c.getId());
                workerRepository.deleteByCompanyId(c.getId());
                companyRepository.delete(c);
                excess--;
            }
        }
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
