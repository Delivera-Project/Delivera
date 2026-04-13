package com.delivera.dto.order;

import com.delivera.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicOrderResponse(
        UUID id,
        String reference,
        String companyName,
        String originName,
        String destinationName,
        String recipientName,
        String status,
        String priority,
        Instant createdAt,
        List<OrderEventResponse> events,
        boolean claimable,
        String recipientEmailHint) {

    public static PublicOrderResponse from(Order order) {
        boolean claimable = order.getTrackingToken() != null
                && (order.getLoyalUser() == null || order.getLoyalUser().getUser() == null);
        String hint = claimable && order.getRecipientEmail() != null
                ? maskEmail(order.getRecipientEmail()) : null;
        return new PublicOrderResponse(
                order.getId(),
                order.getReference(),
                order.getCompany().getName(),
                order.getOrigin().getName(),
                order.getDestination() != null ? order.getDestination().getName() : null,
                order.getRecipientName(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getCreatedAt(),
                order.getEvents().stream().map(OrderEventResponse::from).toList(),
                claimable,
                hint);
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
