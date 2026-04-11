package com.delivera.dto.unit;

import com.delivera.model.OperationalUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UnitDetailResponse(
        UUID id,
        String name,
        String type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt,
        List<UnitWorkerSummary> workers) {

    public static UnitDetailResponse from(OperationalUnit unit) {
        return new UnitDetailResponse(
                unit.getId(),
                unit.getName(),
                unit.getType().name(),
                unit.getAddress(),
                unit.getLatitude(),
                unit.getLongitude(),
                unit.getCreatedAt(),
                unit.getWorkers().stream().map(UnitWorkerSummary::from).toList());
    }
}
