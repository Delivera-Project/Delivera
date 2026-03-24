package com.delivera.dto.order;

import com.delivera.model.Order;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String reference,
        String orderType,
        UUID originId,
        String originName,
        UUID destinationId,
        String destinationName,
        String recipientEmail,
        String recipientName,
        String status,
        String priority,
        String notes,
        String trackingToken,
        boolean claimed,
        Instant createdAt) {

    public static OrderResponse from(Order order) {
        var lu = order.getLoyalUser();
        boolean claimed = lu != null && lu.getUser() != null;
        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getOrderType().name(),
                order.getOrigin().getId(),
                order.getOrigin().getName(),
                order.getDestination() != null ? order.getDestination().getId() : null,
                order.getDestination() != null ? order.getDestination().getName() : null,
                order.getRecipientEmail(),
                order.getRecipientName(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getNotes(),
                order.getTrackingToken(),
                claimed,
                order.getCreatedAt());
    }
}
