package com.delivera.dto.order;

import com.delivera.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatusRequest(
        @NotNull OrderStatus status,
        @Size(max = 1000) String note) {}
