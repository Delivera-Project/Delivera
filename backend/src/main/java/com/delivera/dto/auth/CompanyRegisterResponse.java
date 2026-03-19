package com.delivera.dto.auth;

import java.util.UUID;

public record CompanyRegisterResponse(
        String token,
        String email,
        UUID companyId,
        String organizationSlug,
        String role
) {}
