package com.hireflow.dto.request;

import com.hireflow.domain.enums.InterviewType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Request body for scheduling an interview.
 */
@Data
public class CreateInterviewRequest {

    @NotNull(message = "Application ID must not be null")
    private UUID applicationId;

    @NotNull(message = "Interview type is required")
    private InterviewType interviewType = InterviewType.VIDEO;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private Instant scheduledAt;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    private short durationMinutes = 60;

    private String meetingLink;

    private String locationNotes;
}
