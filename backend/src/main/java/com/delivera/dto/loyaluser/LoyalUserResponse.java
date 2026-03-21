package com.delivera.dto.loyaluser;

import com.delivera.model.LoyalUser;

import java.time.Instant;
import java.util.UUID;

public record LoyalUserResponse(
        UUID id,
        String email,
        boolean registered,
        Instant createdAt) {

    public static LoyalUserResponse from(LoyalUser lu) {
        return new LoyalUserResponse(lu.getId(), lu.getEmail(), lu.getUser() != null, lu.getCreatedAt());
    }
}
