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
        UUID originCompanyId,
        UUID destinationId,
        String destinationName,
        UUID destinationCompanyId,
        String recipientEmail,
        String recipientName,
        String recipientAddress,
        String status,
        String priority,
        String notes,
        String trackingToken,
        boolean claimed,
        UUID loyalUserId,
        Instant createdAt,
        Double originLat,
        Double originLon,
        Double destinationLat,
        Double destinationLon) {

    public static OrderResponse from(Order order) {
        var lu = order.getLoyalUser();
        boolean claimed = lu != null && lu.getUser() != null;
        Double destLat = order.getDestination() != null && order.getDestination().getLatitude() != null
                ? Double.valueOf(order.getDestination().getLatitude().doubleValue())
                : (order.getRecipientLatitude() != null ? Double.valueOf(order.getRecipientLatitude().doubleValue()) : null);
        Double destLon = order.getDestination() != null && order.getDestination().getLongitude() != null
                ? Double.valueOf(order.getDestination().getLongitude().doubleValue())
                : (order.getRecipientLongitude() != null ? Double.valueOf(order.getRecipientLongitude().doubleValue()) : null);
        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getOrderType().name(),
                order.getOrigin().getId(),
                order.getOrigin().getName(),
                order.getOrigin().getCompany() != null ? order.getOrigin().getCompany().getId() : null,
                order.getDestination() != null ? order.getDestination().getId() : null,
                order.getDestination() != null ? order.getDestination().getName() : null,
                order.getDestination() != null && order.getDestination().getCompany() != null ? order.getDestination().getCompany().getId() : null,
                order.getRecipientEmail(),
                order.getRecipientName(),
                order.getRecipientAddress(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getNotes(),
                order.getTrackingToken(),
                claimed,
                lu != null ? lu.getId() : null,
                order.getCreatedAt(),
                order.getOrigin().getLatitude() != null ? order.getOrigin().getLatitude().doubleValue() : null,
                order.getOrigin().getLongitude() != null ? order.getOrigin().getLongitude().doubleValue() : null,
                destLat,
                destLon);
    }
}
