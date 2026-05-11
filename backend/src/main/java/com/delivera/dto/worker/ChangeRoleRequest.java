package com.delivera.dto.worker;

import com.delivera.model.WorkerRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull WorkerRole role) {}
