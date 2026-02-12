package com.delivera.controller;

import com.delivera.dto.auth.LoginRequest;
import com.delivera.dto.auth.LoginResponse;
import com.delivera.dto.auth.RegisterRequest;
import com.delivera.dto.auth.RegisterResponse;
import com.delivera.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request.email(), request.password());
        return ResponseEntity.ok(response);
    }
}
