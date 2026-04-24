package com.delivera.exception;

public class ApiKeyNotFoundException extends RuntimeException {
    public ApiKeyNotFoundException() {
        super("API key not found");
    }
}
