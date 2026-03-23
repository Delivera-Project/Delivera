package com.delivera.exception;

public class OrderAlreadyClaimedException extends RuntimeException {
    public OrderAlreadyClaimedException() {
        super("Order already claimed by a registered user");
    }
}
