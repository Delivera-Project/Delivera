package com.delivera.controller;

import com.delivera.dto.auth.LoginRequest;
import com.delivera.dto.auth.LoginResponse;
import com.delivera.dto.auth.RegisterRequest;
import com.delivera.dto.auth.RegisterResponse;
import com.delivera.security.SecurityUtils;
import com.delivera.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private SecurityUtils securityUtils;
    @InjectMocks
    private AuthController authController;

    @Test
    void login_returns200WithLoginResponse() {
        LoginRequest req = new LoginRequest("user@test.com", "Password1");
        LoginResponse loginResponse = new LoginResponse("jwt-token", "user@test.com", null, "LOYAL_USER", null, null, null);
        when(authService.login("user@test.com", "Password1")).thenReturn(loginResponse);

        var response = authController.login(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(loginResponse);
    }

    @Test
    void register_returns200WithRegisterResponse() {
        RegisterRequest req = new RegisterRequest("new@test.com", "newuser", "John", null, null, "Password1");
        RegisterResponse registerResponse = new RegisterResponse("reg-token", "new@test.com");
        when(authService.register(req)).thenReturn(registerResponse);

        var response = authController.register(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().token()).isEqualTo("reg-token");
    }
}
