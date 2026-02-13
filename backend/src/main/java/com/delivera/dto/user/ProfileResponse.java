package com.delivera.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProfileResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Instant createdAt;
}
