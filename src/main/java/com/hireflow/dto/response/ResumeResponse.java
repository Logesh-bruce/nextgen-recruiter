package com.hireflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private UUID id;
    private String fileName;
    private String parseStatus;
    private boolean isPrimary;
    private Instant createdAt;
    private List<String> skills;
    private List<ResumeExperienceResponse> experiences;
    private List<ResumeEducationResponse> educations;
}
