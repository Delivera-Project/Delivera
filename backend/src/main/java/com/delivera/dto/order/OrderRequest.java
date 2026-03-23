package com.delivera.dto.order;

import com.delivera.model.OrderPriority;
import com.delivera.model.OrderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OrderRequest(
        @NotNull UUID originId,
        UUID destinationId,
        @Email @Size(max = 255) String recipientEmail,
        @Size(max = 255) String recipientName,
        OrderType orderType,
        OrderPriority priority,
        @Size(max = 1000) String notes) {}
