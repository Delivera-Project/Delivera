package com.delivera.dto.unit;

import com.delivera.model.UnitType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UnitRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        UnitType type,

        @Size(max = 500)
        String address,

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal longitude
) {
    @AssertTrue(message = "Latitude and longitude must both be provided or both be absent")
    public boolean isCoordinatesConsistent() {
        return (latitude == null) == (longitude == null);
    }
}
