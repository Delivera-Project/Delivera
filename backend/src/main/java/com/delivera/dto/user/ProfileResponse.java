package com.delivera.dto.user;

import java.time.Instant;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Instant createdAt
) {}
