package com.delivera.controller;

import com.delivera.dto.auth.ClaimRegisterRequest;
import com.delivera.dto.auth.LoginResponse;
import com.delivera.service.AuthService;
import com.delivera.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private AuthService authService;
    @InjectMocks private OrderController orderController;

    @Test
    void claimRegister_returns201WithLoginResponse() {
        ClaimRegisterRequest req = new ClaimRegisterRequest("John", "Doe", "john@test.com", "Password1");
        LoginResponse loginResponse = new LoginResponse("jwt-token", "john@test.com", null, "LOYAL_USER", null, null, null);
        when(authService.claimRegister("abc123", req)).thenReturn(loginResponse);

        var response = orderController.claimRegister("abc123", req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(loginResponse);
    }
}
