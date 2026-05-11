package com.delivera.controller;

import com.delivera.dto.loyaluser.LoyalUserRequest;
import com.delivera.dto.loyaluser.LoyalUserResponse;
import com.delivera.service.LoyalUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyalUserControllerTest {

    @Mock private LoyalUserService loyalUserService;
    @InjectMocks private LoyalUserController controller;

    // LoyalUserResponse(id, email, name, phone, linkedAccount, orderCount, address, lat, lon, createdAt)
    private static LoyalUserResponse sampleLoyalUser() {
        return new LoyalUserResponse(UUID.randomUUID(), "lu@e.com", "Lu User", null, false, 0L, null, null, null, null);
    }

    @Test
    void list_returns200() {
        when(loyalUserService.getByCompany()).thenReturn(List.of(sampleLoyalUser()));
        var resp = controller.list();
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void add_returns201() {
        // LoyalUserRequest(email, address, firstName, lastName, lat, lon)
        LoyalUserRequest req = new LoyalUserRequest("lu@e.com", null, null, null, null, null);
        LoyalUserResponse expected = sampleLoyalUser();
        when(loyalUserService.add(req)).thenReturn(expected);
        var resp = controller.add(req);
        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        assertThat(resp.getBody()).isSameAs(expected);
    }

    @Test
    void updateAddress_returns200() {
        UUID id = UUID.randomUUID();
        LoyalUserRequest req = new LoyalUserRequest("lu@e.com", "Calle 1", null, null, null, null);
        LoyalUserResponse expected = sampleLoyalUser();
        when(loyalUserService.updateAddress(id, req)).thenReturn(expected);
        assertThat(controller.updateAddress(id, req).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void orders_returns200() {
        UUID id = UUID.randomUUID();
        when(loyalUserService.getOrdersForLoyalUser(id)).thenReturn(List.of());
        assertThat(controller.orders(id).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void myOrders_returns200() {
        when(loyalUserService.getMyOrders()).thenReturn(List.of());
        assertThat(controller.myOrders().getStatusCode().value()).isEqualTo(200);
    }
}
