package com.hireflow.dto.request;

import com.hireflow.domain.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for updating a job's status (DRAFT, ACTIVE, PAUSED, CLOSED).
 */
@Data
public class UpdateJobStatusRequest {

    @NotNull(message = "Status must not be null")
    private JobStatus status;
}
