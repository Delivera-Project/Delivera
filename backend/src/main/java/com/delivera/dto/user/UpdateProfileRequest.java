package com.delivera.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProfileRequest(
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-z0-9_-]+$", message = "Username only allows lowercase letters, numbers, hyphens and underscores")
        String username,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 20)
        String phone,

        @Size(max = 500)
        String address,

        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        BigDecimal longitude
) {
    @AssertTrue(message = "Latitude and longitude must both be provided or both be absent")
    public boolean isCoordinatesConsistent() {
        return (latitude == null) == (longitude == null);
    }
}
