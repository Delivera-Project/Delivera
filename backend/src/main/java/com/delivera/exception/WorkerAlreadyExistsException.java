package com.delivera.exception;

public class WorkerAlreadyExistsException extends RuntimeException {
    public WorkerAlreadyExistsException() {
        super("Worker already exists in this company");
    }
}
