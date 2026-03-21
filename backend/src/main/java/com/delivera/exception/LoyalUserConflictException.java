package com.delivera.exception;

public class LoyalUserConflictException extends RuntimeException {
    public LoyalUserConflictException() {
        super("Loyal user already exists for this company");
    }
}
