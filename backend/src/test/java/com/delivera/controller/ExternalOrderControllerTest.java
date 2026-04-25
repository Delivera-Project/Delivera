package com.delivera.controller;

import com.delivera.dto.order.OrderDetailResponse;
import com.delivera.dto.order.OrderLocationRequest;
import com.delivera.dto.order.OrderRequest;
import com.delivera.dto.order.OrderResponse;
import com.delivera.dto.order.OrderStatusRequest;
import java.math.BigDecimal;
import com.delivera.model.OrderStatus;
import com.delivera.model.OrderType;
import com.delivera.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalOrderControllerTest {

    @Mock private OrderService orderService;
    @InjectMocks private ExternalOrderController controller;

    @Test
    void delegatesCreationToOrderServiceAndReturns201() {
        OrderRequest req = new OrderRequest(UUID.randomUUID(), null, "a@b.com", "John",
                "calle", null, null, OrderType.B2C, null, null);
        OrderResponse expected = new OrderResponse(UUID.randomUUID(), null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null);
        when(orderService.create(req)).thenReturn(expected);

        var resp = controller.create(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        assertThat(resp.getBody()).isSameAs(expected);
        verify(orderService).create(req);
    }

    @Test
    void delegatesStatusUpdateToOrderService() {
        UUID id = UUID.randomUUID();
        OrderStatusRequest req = new OrderStatusRequest(OrderStatus.IN_TRANSIT, "actualizado por ERP");
        OrderDetailResponse expected = new OrderDetailResponse(id, null, null, null, null, null, null,
                null, null, null, null, null, null, null, "IN_TRANSIT", null, null, false, null, null,
                null, null, null, null, null, null, null, null, null);
        when(orderService.updateStatus(id, req)).thenReturn(expected);

        var resp = controller.updateStatus(id, req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(expected);
        verify(orderService).updateStatus(id, req);
    }

    @Test
    void delegatesLocationUpdateToOrderService() {
        UUID id = UUID.randomUUID();
        OrderLocationRequest req = new OrderLocationRequest(new BigDecimal("40.4168"), new BigDecimal("-3.7038"));
        OrderDetailResponse expected = new OrderDetailResponse(id, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, false, null, null,
                null, null, null, null, null, null, null, null, null);
        when(orderService.updateLocation(id, req)).thenReturn(expected);

        var resp = controller.updateLocation(id, req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(expected);
        verify(orderService).updateLocation(id, req);
    }
}
