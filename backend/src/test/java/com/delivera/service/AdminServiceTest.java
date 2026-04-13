package com.delivera.service;

import com.delivera.dto.admin.GlobalMetrics;
import com.delivera.dto.admin.OrganizationSummary;
import com.delivera.model.Organization;
import com.delivera.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private WorkerRepository workerRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AdminService adminService;

    @Test
    void listOrganizations_returnsEmptyList() {
        when(organizationRepository.findAll()).thenReturn(List.of());

        List<OrganizationSummary> result = adminService.listOrganizations();

        assertThat(result).isEmpty();
    }

    @Test
    void listOrganizations_returnsSummariesWithMetrics() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setName("Test Org");
        org.setHandle("test-org");
        when(organizationRepository.findAll()).thenReturn(List.of(org));
        when(companyRepository.countByOrganizationId(org.getId())).thenReturn(3L);
        when(workerRepository.countByOrganizationId(org.getId())).thenReturn(10L);
        when(orderRepository.countByOrganizationId(org.getId())).thenReturn(50L);

        List<OrganizationSummary> result = adminService.listOrganizations();

        assertThat(result).hasSize(1);
        OrganizationSummary summary = result.get(0);
        assertThat(summary.id()).isEqualTo(org.getId());
        assertThat(summary.name()).isEqualTo("Test Org");
        assertThat(summary.handle()).isEqualTo("test-org");
        assertThat(summary.companyCount()).isEqualTo(3);
        assertThat(summary.workerCount()).isEqualTo(10);
        assertThat(summary.orderCount()).isEqualTo(50);
    }

    @Test
    void listOrganizations_multipleOrgs() {
        Organization org1 = new Organization();
        org1.setId(UUID.randomUUID());
        org1.setName("Org A");
        org1.setHandle("org-a");
        Organization org2 = new Organization();
        org2.setId(UUID.randomUUID());
        org2.setName("Org B");
        org2.setHandle("org-b");

        when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));
        when(companyRepository.countByOrganizationId(any())).thenReturn(1L);
        when(workerRepository.countByOrganizationId(any())).thenReturn(2L);
        when(orderRepository.countByOrganizationId(any())).thenReturn(5L);

        List<OrganizationSummary> result = adminService.listOrganizations();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Org A");
        assertThat(result.get(1).name()).isEqualTo("Org B");
    }

    @Test
    void getGlobalMetrics_returnsCorrectCounts() {
        when(organizationRepository.count()).thenReturn(5L);
        when(companyRepository.count()).thenReturn(12L);
        when(orderRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(100L);
        when(userRepository.count()).thenReturn(42L);

        GlobalMetrics result = adminService.getGlobalMetrics();

        assertThat(result.totalOrganizations()).isEqualTo(5);
        assertThat(result.totalCompanies()).isEqualTo(12);
        assertThat(result.totalOrdersThisMonth()).isEqualTo(100);
        assertThat(result.totalActiveUsers()).isEqualTo(42);
    }

    @Test
    void getGlobalMetrics_zeroCounts() {
        when(organizationRepository.count()).thenReturn(0L);
        when(companyRepository.count()).thenReturn(0L);
        when(orderRepository.countByCreatedAtAfter(any(Instant.class))).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);

        GlobalMetrics result = adminService.getGlobalMetrics();

        assertThat(result.totalOrganizations()).isZero();
        assertThat(result.totalCompanies()).isZero();
        assertThat(result.totalOrdersThisMonth()).isZero();
        assertThat(result.totalActiveUsers()).isZero();
    }
}
