package com.delivera.dto.admin;

import java.time.Instant;
import java.util.UUID;

public record OrganizationSummary(
        UUID id,
        String name,
        String handle,
        Instant createdAt,
        long companyCount,
        long workerCount,
        long orderCount
) {}
