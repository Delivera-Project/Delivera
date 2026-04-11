package com.delivera.dto.worker;

import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull String role) {}
