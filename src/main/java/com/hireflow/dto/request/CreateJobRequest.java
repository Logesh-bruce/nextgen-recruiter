package com.hireflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Request body for creating or updating a job posting.
 */
@Data
public class CreateJobRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description must not be blank")
    private String description;

    private String location;

    private boolean isRemote;

    @NotBlank(message = "Job type must not be blank")
    private String jobType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP

    @DecimalMin(value = "0.0", message = "Salary min must be positive")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "Salary max must be positive")
    private BigDecimal salaryMax;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "USD";

    @Future(message = "Application deadline must be a future date")
    private Instant applicationDeadline;

    @Valid
    private List<JobSkillRequest> skills;
}
