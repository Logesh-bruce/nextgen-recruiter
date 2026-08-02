package com.hireflow.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Canonical error response envelope returned by {@link GlobalExceptionHandler}
 * for every HTTP error. Matches the contract defined in api_contract.md.
 *
 * <pre>
 * {
 *   "timestamp":   "2025-01-15T10:30:00Z",
 *   "status":      400,
 *   "error":       "Bad Request",
 *   "message":     "Human-readable message",
 *   "path":        "/api/v1/jobs",
 *   "traceId":     "abc-123",
 *   "fieldErrors": { "email": "must not be blank" }  // only on 400
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String traceId;

    /** Only present on 400 Bad Request — maps field name → validation message. */
    private final Map<String, String> fieldErrors;
}
