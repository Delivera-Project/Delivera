package com.delivera.dto.settings;

import java.util.UUID;

public record SettingsResponse(
        UUID orgId,
        String orgName,
        String orgHandle,
        UUID companyId,
        String companyName,
        String activityType
) {}
