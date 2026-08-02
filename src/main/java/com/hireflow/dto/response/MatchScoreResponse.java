package com.hireflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MatchScoreResponse DTO representing AI-computed score and skill gap analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreResponse {

    private int score;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String experienceGap;
    private String aiSummary;
}
