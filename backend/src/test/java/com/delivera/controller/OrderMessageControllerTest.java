package com.delivera.controller;

import com.delivera.dto.chat.OrderMessageRequest;
import com.delivera.dto.chat.OrderMessageResponse;
import com.delivera.service.OrderMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMessageControllerTest {

    @Mock private OrderMessageService service;
    @InjectMocks private OrderMessageController controller;

    private final UUID orderId = UUID.randomUUID();

    @Test
    void getMessages_returnsOkWithList() {
        var msg = new OrderMessageResponse(UUID.randomUUID(), UUID.randomUUID(), "Ana López", "Hola", Instant.now());
        when(service.getMessages(orderId)).thenReturn(List.of(msg));

        var result = controller.getMessages(orderId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("Hola");
    }

    @Test
    void getMessages_returnsEmptyList() {
        when(service.getMessages(orderId)).thenReturn(List.of());

        assertThat(controller.getMessages(orderId)).isEmpty();
    }

    @Test
    void sendMessage_returnsCreatedResponse() {
        var request = new OrderMessageRequest("Nuevo mensaje");
        var response = new OrderMessageResponse(UUID.randomUUID(), UUID.randomUUID(), "Ana López", "Nuevo mensaje", Instant.now());
        when(service.sendMessage(orderId, request)).thenReturn(response);

        var result = controller.sendMessage(orderId, request);

        assertThat(result.content()).isEqualTo("Nuevo mensaje");
    }
}
