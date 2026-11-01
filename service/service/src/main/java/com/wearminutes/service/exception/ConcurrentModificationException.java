package com.wearminutes.service.exception;

public class ConcurrentModificationException extends RuntimeException {
    public ConcurrentModificationException(String message) {
        super(message);
    }

    public ConcurrentModificationException(String resource, String action) {
        super(String.format("Conflict: %s was modified by another process during %s. Please refresh and try again.", resource, action));
    }
}