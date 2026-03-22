package com.delivera.dto.loyaluser;

import com.delivera.model.LoyalUser;

import java.time.Instant;
import java.util.UUID;

public record LoyalUserResponse(
        UUID id,
        String email,
        boolean registered,
        long orderCount,
        Instant createdAt) {

    public static LoyalUserResponse from(LoyalUser lu) {
        return new LoyalUserResponse(lu.getId(), lu.getEmail(), lu.getUser() != null, 0, lu.getCreatedAt());
    }

    public static LoyalUserResponse from(LoyalUser lu, long orderCount) {
        return new LoyalUserResponse(lu.getId(), lu.getEmail(), lu.getUser() != null, orderCount, lu.getCreatedAt());
    }
}
