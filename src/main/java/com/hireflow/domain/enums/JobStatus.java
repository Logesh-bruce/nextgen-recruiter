package com.hireflow.domain.enums;

/** Mirrors the PostgreSQL {@code job_status} enum. */
public enum JobStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    CLOSED
}
