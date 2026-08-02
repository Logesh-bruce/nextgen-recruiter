package com.hireflow.event;

import com.hireflow.domain.Application;

/**
 * Event published when a candidate submits a new job application.
 * Listened to by AI service (to calculate match score) and Notification service (to send email/SMS).
 */
public record ApplicationSubmittedEvent(Application application) {
}
