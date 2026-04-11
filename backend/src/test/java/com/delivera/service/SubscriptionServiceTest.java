package com.delivera.service;

import com.delivera.exception.SubscriptionLimitException;
import com.delivera.model.Company;
import com.delivera.model.Organization;
import com.delivera.model.SubscriptionPlan;
import com.delivera.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private OperationalUnitRepository unitRepository;
    @Mock private WorkerRepository workerRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private LoyalUserRepository loyalUserRepository;
    @InjectMocks private SubscriptionService subscriptionService;

    private UUID companyId;
    private Company company;
    private SubscriptionPlan freePlan;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        freePlan = buildPlan("FREE", 1, 3, 5, 50, 20);
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        company = new Company();
        company.setId(companyId);
        company.setPlan(freePlan);
        company.setOrganization(org);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
    }

    @Test
    void checkUnitLimit_belowLimit_passes() {
        when(unitRepository.countByCompanyId(companyId)).thenReturn(2L);
        assertThatCode(() -> subscriptionService.checkUnitLimit(companyId)).doesNotThrowAnyException();
    }

    @Test
    void checkUnitLimit_atLimit_throws() {
        when(unitRepository.countByCompanyId(companyId)).thenReturn(3L);
        assertThatThrownBy(() -> subscriptionService.checkUnitLimit(companyId))
                .isInstanceOf(SubscriptionLimitException.class)
                .hasMessageContaining("units");
    }

    @Test
    void checkOrderLimit_belowLimit_passes() {
        when(orderRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any(Instant.class))).thenReturn(49L);
        assertThatCode(() -> subscriptionService.checkOrderLimit(companyId)).doesNotThrowAnyException();
    }

    @Test
    void checkOrderLimit_atLimit_throws() {
        when(orderRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any(Instant.class))).thenReturn(50L);
        assertThatThrownBy(() -> subscriptionService.checkOrderLimit(companyId))
                .isInstanceOf(SubscriptionLimitException.class)
                .hasMessageContaining("orders");
    }

    @Test
    void checkLoyalUserLimit_belowLimit_passes() {
        when(loyalUserRepository.countByCompaniesId(companyId)).thenReturn(0L);
        assertThatCode(() -> subscriptionService.checkLoyalUserLimit(companyId)).doesNotThrowAnyException();
    }

    @Test
    void checkLoyalUserLimit_atLimit_throws() {
        when(loyalUserRepository.countByCompaniesId(companyId)).thenReturn(20L);
        assertThatThrownBy(() -> subscriptionService.checkLoyalUserLimit(companyId))
                .isInstanceOf(SubscriptionLimitException.class)
                .hasMessageContaining("loyal_users");
    }

    @Test
    void unlimited_plan_never_blocks() {
        SubscriptionPlan pro = buildPlan("PRO", -1, -1, -1, -1, -1);
        company.setPlan(pro);
        when(unitRepository.countByCompanyId(companyId)).thenReturn(9999L);
        assertThatCode(() -> subscriptionService.checkUnitLimit(companyId)).doesNotThrowAnyException();
    }

    // --- helpers ---

    private SubscriptionPlan buildPlan(String code, int maxCompanies, int maxUnits,
                                       int maxWorkers, int maxOrders, int maxLoyalUsers) {
        SubscriptionPlan p = new SubscriptionPlan();
        p.setCode(code);
        p.setName(code);
        p.setMaxCompanies(maxCompanies);
        p.setMaxUnits(maxUnits);
        p.setMaxWorkers(maxWorkers);
        p.setMaxOrdersPerMonth(maxOrders);
        p.setMaxLoyalUsers(maxLoyalUsers);
        return p;
    }
}
