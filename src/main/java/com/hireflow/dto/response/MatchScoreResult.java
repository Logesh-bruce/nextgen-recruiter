package com.hireflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Structured output model returned by {@link com.hireflow.service.ai.AiGateway}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreResult {

    private int score;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String experienceGap;
    private String aiSummary;
    private List<QuestionItem> interviewQuestions;
    private String modelUsed;
    private int promptTokens;
    private int completionTokens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionItem {
        private String question;
        private String category; // technical | behavioral | situational
    }
}
