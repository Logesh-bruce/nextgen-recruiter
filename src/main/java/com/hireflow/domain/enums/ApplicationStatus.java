package com.hireflow.domain.enums;

/** Mirrors the PostgreSQL {@code app_status} enum. */
public enum ApplicationStatus {
    APPLIED,
    REVIEWING,
    SHORTLISTED,
    REJECTED,
    WITHDRAWN
}
