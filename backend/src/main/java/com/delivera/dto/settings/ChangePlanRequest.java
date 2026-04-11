package com.delivera.dto.settings;

import jakarta.validation.constraints.NotBlank;

public record ChangePlanRequest(@NotBlank String planCode) {}
