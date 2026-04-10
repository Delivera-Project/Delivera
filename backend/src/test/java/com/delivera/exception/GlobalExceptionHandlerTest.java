package com.delivera.exception;

import com.delivera.dto.common.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.HttpStatus.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    static Stream<Arguments> errorMappings() {
        return Stream.of(
            arguments(new InvalidCredentialsException(),      UNAUTHORIZED,         "INVALID_CREDENTIALS"),
            arguments(new OrderNotFoundException(),           NOT_FOUND,            "ORDER_NOT_FOUND"),
            arguments(new EmailAlreadyExistsException(),      CONFLICT,             "EMAIL_ALREADY_EXISTS"),
            arguments(new CompanyContextException(),          FORBIDDEN,            "COMPANY_CONTEXT_MISSING"),
            arguments(new InvalidStatusTransitionException(), UNPROCESSABLE_ENTITY, "INVALID_STATUS_TRANSITION")
        );
    }

    @ParameterizedTest
    @MethodSource("errorMappings")
    void handleKnown_returns_mapped_status_and_code(RuntimeException ex, HttpStatus expectedStatus, String expectedCode) {
        ResponseEntity<ErrorResponse> resp = handler.handleKnown(ex);
        assertThat(resp.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(resp.getBody().code()).isEqualTo(expectedCode);
    }

    @Test
    void handleKnown_unknownException_returns500() {
        ResponseEntity<ErrorResponse> resp = handler.handleKnown(new RuntimeException("unexpected"));
        assertThat(resp.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().code()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void handleKnown_subclassOfMappedException_resolvesByAssignability() {
        // Subclase de RuntimeException no registrada directamente — debe devolver 500
        RuntimeException subclass = new OrderNotFoundException() {};
        ResponseEntity<ErrorResponse> resp = handler.handleKnown(subclass);
        assertThat(resp.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(resp.getBody().code()).isEqualTo("ORDER_NOT_FOUND");
    }

}
