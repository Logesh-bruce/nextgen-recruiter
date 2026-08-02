package com.hireflow.service.ai;

import com.hireflow.dto.request.GenerateQuestionsRequest;
import com.hireflow.dto.response.ApplicantRankingResponse;
import com.hireflow.dto.response.GeneratedQuestionsResponse;
import com.hireflow.dto.response.MatchScoreResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for AI scoring, question generation, and applicant ranking endpoints.
 */
public interface AiService {

    MatchScoreResponse getApplicationScore(UUID applicationId, UUID currentUserId);

    void refreshApplicationScore(UUID applicationId, UUID recruiterUserId);

    GeneratedQuestionsResponse generateQuestions(GenerateQuestionsRequest request);

    List<ApplicantRankingResponse> getApplicantRanking(UUID jobId, UUID recruiterUserId, Integer minScore);
}
