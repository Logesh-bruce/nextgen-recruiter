package com.hireflow.dto.request;

import com.hireflow.domain.enums.InterviewStatus;
import lombok.Data;

import java.time.Instant;

/**
 * Request body for updating or rescheduling an existing interview.
 */
@Data
public class UpdateInterviewRequest {

    private Instant scheduledAt;

    private Short durationMinutes;

    private String meetingLink;

    private String locationNotes;

    private InterviewStatus status;

    private String notes;
}
