package com.delivera.exception;

public class SlugConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SlugConflictException(String slug, Throwable cause) {
        super("Could not generate a unique slug for: " + slug, cause);
    }
}
