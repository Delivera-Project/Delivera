package com.delivera.dto.user;

import com.delivera.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String email,
        String username,
        String firstName,
        String lastName,
        String phone,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt,
        String avatarData
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress(),
                user.getLatitude(),
                user.getLongitude(),
                user.getCreatedAt(),
                user.getAvatarData()
        );
    }
}
