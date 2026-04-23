package com.delivera.dto.loyaluser;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LoyalUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 500) String address,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal longitude) {}
