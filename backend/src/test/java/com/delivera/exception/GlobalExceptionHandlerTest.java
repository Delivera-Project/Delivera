package com.delivera.exception;

import com.delivera.dto.common.ErrorResponse;
import com.delivera.dto.common.ValidationErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleInvalidCredentials_returns401() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(new InvalidCredentialsException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void handleUserNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(new UserNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("USER_NOT_FOUND");
    }

    @Test
    void handleInvalidPassword_currentPasswordInvalid_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidPassword(new InvalidPasswordException("CURRENT_PASSWORD_INVALID"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("CURRENT_PASSWORD_INVALID");
    }

    @Test
    void handleInvalidPassword_newPasswordSameAsCurrent_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidPassword(new InvalidPasswordException("NEW_PASSWORD_SAME_AS_CURRENT"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("NEW_PASSWORD_SAME_AS_CURRENT");
    }

    @Test
    void handleEmailAlreadyExists_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleEmailAlreadyExists(new EmailAlreadyExistsException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("EMAIL_ALREADY_EXISTS");
    }

    @Test
    void handleUsernameAlreadyExists_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleUsernameAlreadyExists(new UsernameAlreadyExistsException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("USERNAME_ALREADY_EXISTS");
    }

    @Test
    void handleCompanyContext_returns403() {
        ResponseEntity<ErrorResponse> response = handler.handleCompanyContext(new CompanyContextException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("COMPANY_CONTEXT_MISSING");
    }

    @Test
    void handleInvalidOrderUnits_returns422() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidOrderUnits(new InvalidOrderUnitsException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("INVALID_ORDER_UNITS");
    }

    @Test
    void handleUnitNameConflict_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleUnitNameConflict(new UnitNameConflictException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("UNIT_NAME_CONFLICT");
    }

    @Test
    void handleUnitNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleUnitNotFound(new UnitNotFoundException(java.util.UUID.randomUUID()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("UNIT_NOT_FOUND");
    }

    @Test
    void handleOrderNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleOrderNotFound(new OrderNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    void handleInvalidTransition_returns422() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidTransition(new InvalidStatusTransitionException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("INVALID_STATUS_TRANSITION");
    }

    @Test
    void handleLoyalUserConflict_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleLoyalUserConflict(new LoyalUserConflictException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("LOYAL_USER_ALREADY_EXISTS");
    }

    @Test
    void handleCompanyHasActiveOrders_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleCompanyHasActiveOrders(new CompanyHasActiveOrdersException(java.util.UUID.randomUUID()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("COMPANY_HAS_ACTIVE_ORDERS");
    }

    @Test
    void handleHandleConflict_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleHandleConflict(new HandleConflictException("slug", null));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("HANDLE_CONFLICT");
    }

    @Test
    void handleOrderAlreadyClaimed_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleOrderAlreadyClaimed(new OrderAlreadyClaimedException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("ORDER_ALREADY_CLAIMED");
    }

    @Test
    void handleOrderClaimEmailMismatch_returns422() {
        ResponseEntity<ErrorResponse> response = handler.handleOrderClaimEmailMismatch(new OrderClaimEmailMismatchException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("ORDER_CLAIM_EMAIL_MISMATCH");
    }

    @Test
    void handleForbidden_returns403() {
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(new ForbiddenException("test"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("FORBIDDEN");
    }

    @Test
    void handleGeneral_returns500() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(new RuntimeException("unexpected"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void handleDataIntegrity_noSqlCause_returns409DataIntegrityError() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("generic violation");
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("DATA_INTEGRITY_ERROR");
    }

    @Test
    void handleValidation_returns400WithFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ValidationErrorResponse> response = handler.handleValidation(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().errors()).hasSize(1);
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("email");
    }
}
