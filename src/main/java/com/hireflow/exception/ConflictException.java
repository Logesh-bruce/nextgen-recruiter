package com.hireflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to create a resource that already exists —
 * e.g., duplicate email registration, double-applying to a job.
 * Maps to HTTP 409 Conflict via {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
