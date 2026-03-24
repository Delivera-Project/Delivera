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
        String originCompanyName,
        UUID destinationId,
        String destinationName,
        String destinationCompanyName,
        String recipientEmail,
        String recipientName,
        String status,
        String priority,
        String trackingToken,
        boolean claimed,
        String notes,
        UUID loyalUserId,
        Instant createdAt,
        List<OrderEventResponse> events) {

    public static OrderDetailResponse from(Order order) {
        var lu = order.getLoyalUser();
        var dest = order.getDestination();
        return new OrderDetailResponse(
                order.getId(),
                order.getReference(),
                order.getOrderType().name(),
                order.getOrigin().getId(),
                order.getOrigin().getName(),
                order.getOrigin().getCompany().getName(),
                dest != null ? dest.getId() : null,
                dest != null ? dest.getName() : null,
                dest != null ? dest.getCompany().getName() : null,
                order.getRecipientEmail(),
                order.getRecipientName(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getTrackingToken(),
                lu != null && lu.getUser() != null,
                order.getNotes(),
                order.getLoyalUser() != null ? order.getLoyalUser().getId() : null,
                order.getCreatedAt(),
                order.getEvents().stream().map(OrderEventResponse::from).toList());
    }
}
