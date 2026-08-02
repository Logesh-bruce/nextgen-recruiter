package com.hireflow.dto.response;

import com.hireflow.domain.enums.InterviewStatus;
import com.hireflow.domain.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed Interview response DTO matching api_contract.md.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {

    private UUID id;
    private UUID applicationId;
    private String jobTitle;
    private String candidateName;
    private InterviewType interviewType;
    private InterviewStatus status;
    private Instant scheduledAt;
    private Short durationMinutes;
    private String meetingLink;
    private String locationNotes;
    private String notes;
    private Instant createdAt;
}
