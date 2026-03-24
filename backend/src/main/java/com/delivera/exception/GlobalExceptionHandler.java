package com.delivera.exception;

import com.delivera.dto.common.ErrorResponse;
import com.delivera.dto.common.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("USER_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Password error: {}", ex.getCode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getCode()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Duplicate email registration attempt");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("EMAIL_ALREADY_EXISTS"));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        log.warn("Duplicate username registration attempt");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("USERNAME_ALREADY_EXISTS"));
    }

    @ExceptionHandler(CompanyContextException.class)
    public ResponseEntity<ErrorResponse> handleCompanyContext(CompanyContextException ex) {
        log.warn("Request with no company context in token");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("COMPANY_CONTEXT_MISSING"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof java.sql.SQLException sqlEx
                    && "23514".equals(sqlEx.getSQLState())) {
                String msg = sqlEx.getMessage();
                if (msg != null && msg.contains("does not belong to company")) {
                    log.warn("Order units company violation: {}", msg);
                    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body(new ErrorResponse("INVALID_ORDER_UNITS"));
                }
            }
            cause = cause.getCause();
        }
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DATA_INTEGRITY_ERROR"));
    }

    @ExceptionHandler(InvalidOrderUnitsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderUnits(InvalidOrderUnitsException ex) {
        log.warn("Invalid order units: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("INVALID_ORDER_UNITS"));
    }

    @ExceptionHandler(UnitNameConflictException.class)
    public ResponseEntity<ErrorResponse> handleUnitNameConflict(UnitNameConflictException ex) {
        log.warn("Unit name conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("UNIT_NAME_CONFLICT"));
    }

    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnitNotFound(UnitNotFoundException ex) {
        log.warn("Unit not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("UNIT_NOT_FOUND"));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ORDER_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
        log.warn("Invalid status transition attempt");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("INVALID_STATUS_TRANSITION"));
    }

    @ExceptionHandler(LoyalUserConflictException.class)
    public ResponseEntity<ErrorResponse> handleLoyalUserConflict(LoyalUserConflictException ex) {
        log.warn("Loyal user already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("LOYAL_USER_ALREADY_EXISTS"));
    }

    @ExceptionHandler(CompanyHasActiveOrdersException.class)
    public ResponseEntity<ErrorResponse> handleCompanyHasActiveOrders(CompanyHasActiveOrdersException ex) {
        log.warn("Cannot delete company with active orders: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("COMPANY_HAS_ACTIVE_ORDERS"));
    }

    @ExceptionHandler(HandleConflictException.class)
    public ResponseEntity<ErrorResponse> handleHandleConflict(HandleConflictException ex) {
        log.error("Handle conflict unresolved after max retries: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("HANDLE_CONFLICT"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
                ))
                .toList();

        log.debug("Validation error: {}", errors);
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(OrderAlreadyClaimedException.class)
    public ResponseEntity<ErrorResponse> handleOrderAlreadyClaimed(OrderAlreadyClaimedException ex) {
        log.warn("Order already claimed");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ORDER_ALREADY_CLAIMED"));
    }

    @ExceptionHandler(OrderClaimEmailMismatchException.class)
    public ResponseEntity<ErrorResponse> handleOrderClaimEmailMismatch(OrderClaimEmailMismatchException ex) {
        log.warn("Order claim email mismatch");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("ORDER_CLAIM_EMAIL_MISMATCH"));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        log.warn("Forbidden access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR"));
    }
}
