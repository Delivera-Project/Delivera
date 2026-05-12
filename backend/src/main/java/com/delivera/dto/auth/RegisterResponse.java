package com.delivera.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterResponse {
    private String token;
    private String email;
    private String role;

    public RegisterResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }
}
