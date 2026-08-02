package com.hireflow.event;

import com.hireflow.domain.Application;
import com.hireflow.domain.enums.ApplicationStatus;

/**
 * Event published when an application status changes (e.g. SHORTLISTED, REJECTED).
 */
public record ApplicationStatusChangedEvent(Application application, ApplicationStatus oldStatus) {
}
