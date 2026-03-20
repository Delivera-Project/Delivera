package com.delivera.dto.order;

import com.delivera.model.Order;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String reference,
        UUID originId,
        String originName,
        UUID destinationId,
        String destinationName,
        String status,
        String notes,
        Instant createdAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getOrigin().getId(),
                order.getOrigin().getName(),
                order.getDestination().getId(),
                order.getDestination().getName(),
                order.getStatus().name(),
                order.getNotes(),
                order.getCreatedAt());
    }
}
