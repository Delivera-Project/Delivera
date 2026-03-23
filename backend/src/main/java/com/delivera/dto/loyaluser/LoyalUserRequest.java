package com.delivera.dto.loyaluser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoyalUserRequest(
        @NotBlank @Email @Size(max = 255) String email) {}
