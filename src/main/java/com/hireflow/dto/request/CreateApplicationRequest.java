package com.hireflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request body for submitting a job application.
 */
@Data
public class CreateApplicationRequest {

    @NotNull(message = "Job ID must not be null")
    private UUID jobId;

    private UUID resumeId;

    private String coverLetter;
}
