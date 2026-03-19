package com.delivera.dto.auth;

import java.util.UUID;

public record LoginResponse(String token, String email, UUID companyId, String role) {}
