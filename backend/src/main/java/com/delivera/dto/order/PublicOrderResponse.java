package com.delivera.dto.order;

import com.delivera.model.Order;

import java.time.Instant;
import java.util.List;

public record PublicOrderResponse(
        String reference,
        String companyName,
        String originName,
        String destinationName,
        String recipientName,
        String status,
        String priority,
        Instant createdAt,
        List<OrderEventResponse> events) {

    public static PublicOrderResponse from(Order order) {
        return new PublicOrderResponse(
                order.getReference(),
                order.getCompany().getName(),
                order.getOrigin().getName(),
                order.getDestination() != null ? order.getDestination().getName() : null,
                order.getRecipientName(),
                order.getStatus().name(),
                order.getPriority().name(),
                order.getCreatedAt(),
                order.getEvents().stream().map(OrderEventResponse::from).toList());
    }
}
