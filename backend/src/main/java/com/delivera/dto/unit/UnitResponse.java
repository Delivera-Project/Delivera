package com.delivera.dto.unit;

import com.delivera.model.OperationalUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UnitResponse(
        UUID id,
        String name,
        String type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt) {

    public static UnitResponse from(OperationalUnit unit) {
        return new UnitResponse(
                unit.getId(),
                unit.getName(),
                unit.getType().name(),
                unit.getAddress(),
                unit.getLatitude(),
                unit.getLongitude(),
                unit.getCreatedAt());
    }
}
