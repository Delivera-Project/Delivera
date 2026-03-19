package com.delivera.controller;

import com.delivera.dto.auth.CompanyRegisterRequest;
import com.delivera.dto.auth.CompanyRegisterResponse;
import com.delivera.dto.auth.LoginRequest;
import com.delivera.dto.auth.LoginResponse;
import com.delivera.dto.auth.RegisterRequest;
import com.delivera.dto.auth.RegisterResponse;
import com.delivera.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro e inicio de sesión")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autenticación de usuario con email y contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Registrar usuario", description = "Crear una nueva cuenta de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Registrar empresa", description = "Crear empresa con su organización y cuenta de administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    @PostMapping("/register/company")
    public ResponseEntity<CompanyRegisterResponse> registerCompany(@Valid @RequestBody CompanyRegisterRequest request) {
        CompanyRegisterResponse response = authService.registerCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
