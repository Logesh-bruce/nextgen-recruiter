package com.hireflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantRankingResponse {

    private int rank;
    private UUID applicationId;
    private String candidateName;
    private int matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
}
