package com.hireflow.dto.response;

import com.hireflow.domain.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed Application response DTO matching api_contract.md.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private CandidateSummaryResponse candidate;
    private UUID resumeId;
    private String coverLetter;
    private ApplicationStatus status;
    private Instant statusUpdatedAt;
    private Instant appliedAt;
    private MatchScoreResponse matchScore;
}
