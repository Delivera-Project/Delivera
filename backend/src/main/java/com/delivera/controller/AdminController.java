package com.delivera.controller;

import com.delivera.dto.admin.GlobalMetrics;
import com.delivera.dto.admin.OrganizationSummary;
import com.delivera.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Panel de administración global")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Lista de organizaciones con métricas")
    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationSummary>> listOrganizations() {
        return ResponseEntity.ok(adminService.listOrganizations());
    }

    @Operation(summary = "Métricas globales de la plataforma")
    @GetMapping("/metrics")
    public ResponseEntity<GlobalMetrics> getGlobalMetrics() {
        return ResponseEntity.ok(adminService.getGlobalMetrics());
    }
}
