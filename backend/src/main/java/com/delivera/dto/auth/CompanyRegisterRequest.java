package com.delivera.dto.auth;

import com.delivera.model.ActivityType;
import jakarta.validation.constraints.*;

public record CompanyRegisterRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")
        String password,

        @NotBlank @Size(max = 255)
        String companyName,

        @NotNull
        ActivityType activityType
) {}
