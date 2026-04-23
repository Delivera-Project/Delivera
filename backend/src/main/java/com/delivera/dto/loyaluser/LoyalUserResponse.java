package com.delivera.dto.loyaluser;

import com.delivera.model.LoyalUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoyalUserResponse(
        UUID id,
        String email,
        boolean registered,
        long orderCount,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt) {

    public static LoyalUserResponse from(LoyalUser lu) {
        return from(lu, 0);
    }

    public static LoyalUserResponse from(LoyalUser lu, long orderCount) {
        BigDecimal addrLat = lu.getLatitude();
        BigDecimal addrLon = lu.getLongitude();
        String addr = lu.getAddress();
        // Si no tiene dirección propia pero el usuario registrado sí, exponerla
        if (lu.getUser() != null) {
            if (addr == null) addr = lu.getUser().getAddress();
            if (addrLat == null) addrLat = lu.getUser().getLatitude();
            if (addrLon == null) addrLon = lu.getUser().getLongitude();
        }
        return new LoyalUserResponse(lu.getId(), lu.getEmail(), lu.getUser() != null, orderCount,
                addr, addrLat, addrLon, lu.getCreatedAt());
    }
}
