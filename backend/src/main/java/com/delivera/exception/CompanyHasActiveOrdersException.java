package com.delivera.exception;

import java.util.UUID;

public class CompanyHasActiveOrdersException extends RuntimeException {
    public CompanyHasActiveOrdersException(UUID companyId) {
        super("Company has active orders: " + companyId);
    }
}
