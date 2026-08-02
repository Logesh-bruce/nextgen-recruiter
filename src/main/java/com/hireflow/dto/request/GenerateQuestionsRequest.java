package com.hireflow.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Request body for on-demand AI interview question generation.
 */
@Data
public class GenerateQuestionsRequest {

    @NotBlank(message = "Job title must not be blank")
    private String jobTitle;

    private List<String> skills;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Count cannot exceed 20")
    private int count = 5;
}
