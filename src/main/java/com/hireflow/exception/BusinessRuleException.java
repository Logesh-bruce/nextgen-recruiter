package com.hireflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated — e.g., applying to a closed job,
 * withdrawing an already-rejected application.
 * Maps to HTTP 422 Unprocessable Entity via {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
