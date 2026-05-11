package com.delivera.dto.loyaluser;

import com.delivera.model.LoyalUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoyalUserResponse(
        UUID id,
        String email,
        String name,
        String phone,
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
        String name = lu.getName();
        String phone = lu.getPhone();
        if (lu.getUser() != null) {
            if (addr == null) addr = lu.getUser().getAddress();
            if (addrLat == null) addrLat = lu.getUser().getLatitude();
            if (addrLon == null) addrLon = lu.getUser().getLongitude();
            if (name == null) name = lu.getUser().getFirstName() != null
                    ? (lu.getUser().getFirstName() + (lu.getUser().getLastName() != null ? " " + lu.getUser().getLastName() : "")).trim()
                    : null;
            if (phone == null) phone = lu.getUser().getPhone();
        }
        return new LoyalUserResponse(lu.getId(), lu.getEmail(), name, phone,
                lu.getUser() != null, orderCount, addr, addrLat, addrLon, lu.getCreatedAt());
    }
}
