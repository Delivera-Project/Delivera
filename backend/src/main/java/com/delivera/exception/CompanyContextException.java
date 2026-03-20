package com.delivera.exception;

public class CompanyContextException extends RuntimeException {
    public CompanyContextException() {
        super("No company context in token");
    }
}
