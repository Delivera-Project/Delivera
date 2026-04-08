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

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private record Mapping(HttpStatus status, String code) {}

    // Mapa de excepción → (status HTTP, código de error devuelto al frontend)
    private static final Map<Class<?>, Mapping> ERRORS = Map.ofEntries(
        Map.entry(InvalidCredentialsException.class,      new Mapping(UNAUTHORIZED,         "INVALID_CREDENTIALS")),
        Map.entry(UserNotFoundException.class,            new Mapping(NOT_FOUND,            "USER_NOT_FOUND")),
        Map.entry(EmailAlreadyExistsException.class,      new Mapping(CONFLICT,             "EMAIL_ALREADY_EXISTS")),
        Map.entry(UsernameAlreadyExistsException.class,   new Mapping(CONFLICT,             "USERNAME_ALREADY_EXISTS")),
        Map.entry(CompanyContextException.class,          new Mapping(FORBIDDEN,            "COMPANY_CONTEXT_MISSING")),
        Map.entry(InvalidOrderUnitsException.class,       new Mapping(UNPROCESSABLE_ENTITY, "INVALID_ORDER_UNITS")),
        Map.entry(UnitNameConflictException.class,        new Mapping(CONFLICT,             "UNIT_NAME_CONFLICT")),
        Map.entry(UnitNotFoundException.class,            new Mapping(NOT_FOUND,            "UNIT_NOT_FOUND")),
        Map.entry(OrderNotFoundException.class,           new Mapping(NOT_FOUND,            "ORDER_NOT_FOUND")),
        Map.entry(InvalidStatusTransitionException.class, new Mapping(UNPROCESSABLE_ENTITY, "INVALID_STATUS_TRANSITION")),
        Map.entry(LoyalUserConflictException.class,       new Mapping(CONFLICT,             "LOYAL_USER_ALREADY_EXISTS")),
        Map.entry(CompanyHasActiveOrdersException.class,  new Mapping(CONFLICT,             "COMPANY_HAS_ACTIVE_ORDERS")),
        Map.entry(HandleConflictException.class,          new Mapping(CONFLICT,             "HANDLE_CONFLICT")),
        Map.entry(OrderAlreadyClaimedException.class,     new Mapping(CONFLICT,             "ORDER_ALREADY_CLAIMED")),
        Map.entry(OrderClaimEmailMismatchException.class, new Mapping(UNPROCESSABLE_ENTITY, "ORDER_CLAIM_EMAIL_MISMATCH")),
        Map.entry(ForbiddenException.class,               new Mapping(FORBIDDEN,            "FORBIDDEN"))
    );

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleKnown(RuntimeException ex) {
        Mapping m = ERRORS.get(ex.getClass());
        if (m == null) {
            log.error("Unhandled runtime exception: {}", ex.getMessage(), ex);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ErrorResponse("INTERNAL_ERROR"));
        }
        log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(m.status()).body(new ErrorResponse(m.code()));
    }

    // InvalidPasswordException tiene código dinámico — no encaja en el mapa
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Password validation failed: {}", ex.getCode());
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getCode()));
    }

    // DataIntegrityViolationException requiere inspección del cause para distinguir casos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof java.sql.SQLException sqlEx && "23514".equals(sqlEx.getSQLState())) {
                String msg = sqlEx.getMessage();
                if (msg != null && msg.contains("does not belong to company")) {
                    log.warn("Order units company violation: {}", msg);
                    return ResponseEntity.status(UNPROCESSABLE_ENTITY)
                            .body(new ErrorResponse("INVALID_ORDER_UNITS"));
                }
            }
            cause = cause.getCause();
        }
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        return ResponseEntity.status(CONFLICT).body(new ErrorResponse("DATA_INTEGRITY_ERROR"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ValidationErrorResponse.FieldError(
                        e.getField(),
                        e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value"
                ))
                .toList();
        log.debug("Validation errors: {}", errors);
        return ResponseEntity.badRequest().body(new ValidationErrorResponse("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ErrorResponse("INTERNAL_ERROR"));
    }
}
