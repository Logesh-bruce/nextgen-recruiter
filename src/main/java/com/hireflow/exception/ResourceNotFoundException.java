package com.hireflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource does not exist in the database.
 * Maps to HTTP 404 Not Found via {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience factory — produces a message like "Job not found with id: uuid".
     */
    public static ResourceNotFoundException of(String resourceName, String field, Object value) {
        return new ResourceNotFoundException(
                String.format("%s not found with %s: %s", resourceName, field, value));
    }
}
