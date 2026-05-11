package com.delivera.dto.worker;

import com.delivera.model.Worker;

import java.time.Instant;
import java.util.UUID;

public record WorkerResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant createdAt,
        String tempPassword) {

    public static WorkerResponse from(Worker w) {
        return new WorkerResponse(
                w.getId(),
                w.getUser().getEmail(),
                w.getUser().getFirstName(),
                w.getUser().getLastName(),
                w.getRole().name(),
                w.getCreatedAt(),
                null);
    }

    public static WorkerResponse withTemp(Worker w, String tempPassword) {
        return new WorkerResponse(
                w.getId(),
                w.getUser().getEmail(),
                w.getUser().getFirstName(),
                w.getUser().getLastName(),
                w.getRole().name(),
                w.getCreatedAt(),
                tempPassword);
    }
}
