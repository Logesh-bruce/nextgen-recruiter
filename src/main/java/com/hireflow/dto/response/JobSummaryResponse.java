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
 * Abbreviated Job response DTO for list view endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryResponse {

    private UUID id;
    private String title;
    private String companyName;
    private String location;
    private boolean isRemote;
    private String jobType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private JobStatus status;
    private Instant publishedAt;
    private Instant applicationDeadline;
    private List<String> skills;
}
