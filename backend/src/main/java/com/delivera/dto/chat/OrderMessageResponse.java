package com.delivera.dto.chat;

import com.delivera.model.OrderMessage;

import java.time.Instant;
import java.util.UUID;

public record OrderMessageResponse(
        UUID id,
        UUID senderId,
        String senderName,
        String content,
        Instant createdAt) {

    public static OrderMessageResponse from(OrderMessage m) {
        String name = m.getSender().getFirstName() != null
                ? (m.getSender().getFirstName() + " " + m.getSender().getLastName()).trim()
                : m.getSender().getEmail();
        return new OrderMessageResponse(
                m.getId(),
                m.getSender().getId(),
                name,
                m.getContent(),
                m.getCreatedAt());
    }
}
