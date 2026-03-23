package com.delivera.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
        String phone
) {}
