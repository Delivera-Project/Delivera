package com.delivera.dto.admin;

public record GlobalMetrics(
        long totalOrganizations,
        long totalCompanies,
        long totalOrdersThisMonth,
        long totalActiveUsers
) {}
