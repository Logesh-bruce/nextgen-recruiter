package com.hireflow.dto.request;

import com.hireflow.domain.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for updating application status and recruiter notes.
 */
@Data
public class UpdateApplicationStatusRequest {

    @NotNull(message = "Status must not be null")
    private ApplicationStatus status;

    private String recruiterNotes;
}
