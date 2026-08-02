package com.hireflow.event;

import com.hireflow.domain.Interview;

/**
 * Event published when an interview is scheduled or rescheduled.
 * Listened to by Notification service to send calendar invite emails / SMS alerts.
 */
public record InterviewScheduledEvent(Interview interview) {
}
