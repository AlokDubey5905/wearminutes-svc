package com.wearminutes.service.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with ID: " + id);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName + " not found with " + fieldName + ": " + fieldValue);
    }
}
