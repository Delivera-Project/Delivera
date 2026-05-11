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
        String recipientAddress,
        String status,
        String priority,
        Instant createdAt,
        List<OrderEventResponse> events,
        boolean claimable,
        String recipientEmailHint,
        Double originLat,
        Double originLon,
        Double destinationLat,
        Double destinationLon) {

    public static PublicOrderResponse from(Order order) {
        boolean claimable = order.getTrackingToken() != null
                && (order.getLoyalUser() == null || order.getLoyalUser().getUser() == null);
        String hint = claimable && order.getRecipientEmail() != null
                ? maskEmail(order.getRecipientEmail()) : null;

        var dest = order.getDestination();
        Double destLat = dest != null && dest.getLatitude() != null
                ? Double.valueOf(dest.getLatitude().doubleValue())
                : (order.getRecipientLatitude() != null ? Double.valueOf(order.getRecipientLatitude().doubleValue()) : null);
        Double destLon = dest != null && dest.getLongitude() != null
                ? Double.valueOf(dest.getLongitude().doubleValue())
                : (order.getRecipientLongitude() != null ? Double.valueOf(order.getRecipientLongitude().doubleValue()) : null);

        return new PublicOrderResponse(
                order.getId(),
                order.getReference(),
                order.getCompany().getName(),
                order.getOrigin().getName(),
                dest != null ? dest.getName() : null,
                order.getRecipientName(),
                order.getRecipientAddress(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getCreatedAt(),
                order.getEvents().stream().map(OrderEventResponse::from).toList(),
                claimable,
                hint,
                order.getOrigin().getLatitude() != null ? order.getOrigin().getLatitude().doubleValue() : null,
                order.getOrigin().getLongitude() != null ? order.getOrigin().getLongitude().doubleValue() : null,
                destLat,
                destLon);
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
