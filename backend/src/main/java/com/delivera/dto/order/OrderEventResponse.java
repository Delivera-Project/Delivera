package com.delivera.dto.order;

import com.delivera.model.OrderEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderEventResponse(
        UUID id,
        String status,
        String note,
        String authorEmail,
        Instant createdAt) {

    public static OrderEventResponse from(OrderEvent e) {
        return new OrderEventResponse(e.getId(), e.getStatus().name(), e.getNote(), e.getAuthorEmail(), e.getCreatedAt());
    }
}
