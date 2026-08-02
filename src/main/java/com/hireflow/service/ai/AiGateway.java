package com.hireflow.service.ai;

import com.hireflow.dto.response.GeneratedQuestionsResponse;
import com.hireflow.dto.response.MatchScoreResult;

import java.util.List;

/**
 * AI Gateway interface — adapter pattern decoupling business logic from OpenAI/Gemini SDKs.
 */
public interface AiGateway {

    MatchScoreResult computeMatchScore(String resumeText, String jobTitle, String jobDescription, List<String> requiredSkills);

    GeneratedQuestionsResponse generateInterviewQuestions(String jobTitle, List<String> skills, int count);
}
