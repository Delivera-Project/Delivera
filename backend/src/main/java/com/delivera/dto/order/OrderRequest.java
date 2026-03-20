package com.delivera.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OrderRequest(
        @NotNull UUID originId,
        @NotNull UUID destinationId,
        @Size(max = 1000) String notes) {}
