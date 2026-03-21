package com.delivera.dto.order;

import com.delivera.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        String reference,
        UUID originId,
        String originName,
        UUID destinationId,
        String destinationName,
        String recipientEmail,
        String recipientName,
        String status,
        String priority,
        String trackingToken,
        String notes,
        UUID loyalUserId,
        Instant createdAt,
        List<OrderEventResponse> events) {

    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                order.getReference(),
                order.getOrigin().getId(),
                order.getOrigin().getName(),
                order.getDestination() != null ? order.getDestination().getId() : null,
                order.getDestination() != null ? order.getDestination().getName() : null,
                order.getRecipientEmail(),
                order.getRecipientName(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getTrackingToken(),
                order.getNotes(),
                order.getLoyalUser() != null ? order.getLoyalUser().getId() : null,
                order.getCreatedAt(),
                order.getEvents().stream().map(OrderEventResponse::from).toList());
    }
}
