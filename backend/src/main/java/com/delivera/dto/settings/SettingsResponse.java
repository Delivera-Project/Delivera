package com.delivera.dto.settings;

import com.delivera.model.OrderPriority;

import java.util.UUID;

public record SettingsResponse(
        UUID orgId,
        String orgName,
        String orgHandle,
        UUID companyId,
        String companyName,
        String activityType,
        OrderPriority defaultPriority
) {}
