package com.delivera.controller;

import com.delivera.dto.admin.GlobalMetrics;
import com.delivera.dto.admin.OrganizationSummary;
import com.delivera.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private AdminService adminService;
    @InjectMocks private AdminController adminController;

    @Test
    void listOrganizations_returnsOkWithList() {
        var summary = new OrganizationSummary(UUID.randomUUID(), "Org", "org", Instant.now(), 2, 5, 10);
        when(adminService.listOrganizations()).thenReturn(List.of(summary));

        var response = adminController.listOrganizations();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Org");
    }

    @Test
    void listOrganizations_emptyList() {
        when(adminService.listOrganizations()).thenReturn(List.of());

        var response = adminController.listOrganizations();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getGlobalMetrics_returnsOkWithMetrics() {
        var metrics = new GlobalMetrics(3, 8, 120, 50);
        when(adminService.getGlobalMetrics()).thenReturn(metrics);

        var response = adminController.getGlobalMetrics();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().totalOrganizations()).isEqualTo(3);
        assertThat(response.getBody().totalCompanies()).isEqualTo(8);
        assertThat(response.getBody().totalOrdersThisMonth()).isEqualTo(120);
        assertThat(response.getBody().totalActiveUsers()).isEqualTo(50);
    }
}
