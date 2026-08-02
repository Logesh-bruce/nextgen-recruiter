package com.hireflow.dto.response;

import com.hireflow.domain.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detailed Job response DTO matching api_contract.md.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private UUID id;
    private String title;
    private String description;
    private String companyName;
    private UUID recruiterId;
    private String location;
    private boolean isRemote;
    private String jobType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private JobStatus status;
    private Instant applicationDeadline;
    private Instant publishedAt;
    private Instant createdAt;
    private List<JobSkillResponse> skills;
}
