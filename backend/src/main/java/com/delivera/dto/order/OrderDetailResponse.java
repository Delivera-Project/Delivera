package com.delivera.dto.order;

import com.delivera.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        String reference,
        String orderType,
        UUID originId,
        String originName,
        UUID originCompanyId,
        String originCompanyName,
        UUID destinationId,
        String destinationName,
        UUID destinationCompanyId,
        String destinationCompanyName,
        String recipientEmail,
        String recipientName,
        String recipientAddress,
        String status,
        String priority,
        String trackingToken,
        boolean claimed,
        String notes,
        UUID loyalUserId,
        Instant createdAt,
        List<OrderEventResponse> events,
        Double originLat,
        Double originLon,
        Double destinationLat,
        Double destinationLon,
        Double currentLat,
        Double currentLon,
        Instant currentLocationAt) {

    public static OrderDetailResponse from(Order order) {
        var lu = order.getLoyalUser();
        var dest = order.getDestination();
        Double destLat = dest != null && dest.getLatitude() != null
                ? Double.valueOf(dest.getLatitude().doubleValue())
                : (order.getRecipientLatitude() != null ? Double.valueOf(order.getRecipientLatitude().doubleValue()) : null);
        Double destLon = dest != null && dest.getLongitude() != null
                ? Double.valueOf(dest.getLongitude().doubleValue())
                : (order.getRecipientLongitude() != null ? Double.valueOf(order.getRecipientLongitude().doubleValue()) : null);
        return new OrderDetailResponse(
                order.getId(),
                order.getReference(),
                order.getOrderType().name(),
                order.getOrigin().getId(),
                order.getOrigin().getName(),
                order.getOrigin().getCompany() != null ? order.getOrigin().getCompany().getId() : null,
                order.getOrigin().getCompany() != null ? order.getOrigin().getCompany().getName() : null,
                dest != null ? dest.getId() : null,
                dest != null ? dest.getName() : null,
                dest != null && dest.getCompany() != null ? dest.getCompany().getId() : null,
                dest != null && dest.getCompany() != null ? dest.getCompany().getName() : null,
                order.getRecipientEmail(),
                order.getRecipientName(),
                order.getRecipientAddress(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getTrackingToken(),
                lu != null && lu.getUser() != null,
                order.getNotes(),
                order.getLoyalUser() != null ? order.getLoyalUser().getId() : null,
                order.getCreatedAt(),
                order.getEvents() != null ? order.getEvents().stream().map(OrderEventResponse::from).toList() : List.of(),
                order.getOrigin().getLatitude() != null ? order.getOrigin().getLatitude().doubleValue() : null,
                order.getOrigin().getLongitude() != null ? order.getOrigin().getLongitude().doubleValue() : null,
                destLat,
                destLon,
                order.getCurrentLat() != null ? order.getCurrentLat().doubleValue() : null,
                order.getCurrentLon() != null ? order.getCurrentLon().doubleValue() : null,
                order.getCurrentLocationAt());
    }
}
