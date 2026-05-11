package com.delivera.exception;

public class LoyalUserNotFoundException extends RuntimeException {
    public LoyalUserNotFoundException() {
        super("Loyal user not found");
    }
}
