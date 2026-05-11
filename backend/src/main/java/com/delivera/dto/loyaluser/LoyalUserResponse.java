package com.delivera.dto.loyaluser;

import com.delivera.model.LoyalUser;
import com.delivera.model.User;

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
            User u = lu.getUser();
            addrLat = addrLat != null ? addrLat : u.getLatitude();
            addrLon = addrLon != null ? addrLon : u.getLongitude();
            addr    = addr    != null ? addr    : u.getAddress();
            name    = name    != null ? name    : resolveFullName(u);
            phone   = phone   != null ? phone   : u.getPhone();
        }
        return new LoyalUserResponse(lu.getId(), lu.getEmail(), name, phone,
                lu.getUser() != null, orderCount, addr, addrLat, addrLon, lu.getCreatedAt());
    }

    private static String resolveFullName(User user) {
        if (user.getFirstName() == null) return null;
        String lastName = user.getLastName();
        String suffix = lastName != null ? " " + lastName : "";
        return (user.getFirstName() + suffix).trim();
    }
}
