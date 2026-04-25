package com.delivera.controller;

import com.delivera.dto.order.OrderRequest;
import com.delivera.dto.order.OrderResponse;
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
                null, null, null, null, null, null, null, null, null, null, false, null, null, null, null, null, null);
        when(orderService.create(req)).thenReturn(expected);

        var resp = controller.create(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        assertThat(resp.getBody()).isSameAs(expected);
        verify(orderService).create(req);
    }
}
