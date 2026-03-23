package com.delivera.exception;

public class OrderClaimEmailMismatchException extends RuntimeException {
    public OrderClaimEmailMismatchException() {
        super("Email does not match the order recipient email");
    }
}
