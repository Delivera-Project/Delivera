package com.delivera.dto.unit;

import com.delivera.model.OperationalUnit;

import java.util.UUID;

public record B2BUnitResponse(UUID id, String name, String type, UUID companyId, String companyName) {

    public static B2BUnitResponse from(OperationalUnit unit) {
        return new B2BUnitResponse(
                unit.getId(),
                unit.getName(),
                unit.getType().name(),
                unit.getCompany().getId(),
                unit.getCompany().getName());
    }
}
