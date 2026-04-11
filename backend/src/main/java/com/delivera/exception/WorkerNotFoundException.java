package com.delivera.exception;

public class WorkerNotFoundException extends RuntimeException {
    public WorkerNotFoundException() {
        super("Worker not found");
    }
}
