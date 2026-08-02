package com.hireflow.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireflow.domain.*;
import com.hireflow.dto.response.MatchScoreResult;
import com.hireflow.repository.AiUsageLogRepository;
import com.hireflow.repository.MatchScoreRepository;
import com.hireflow.repository.ResumeRepository;
import com.hireflow.service.ai.AiGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Event listener triggering asynchronous AI match scoring upon application submission.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiEventListener {

    private final AiGateway aiGateway;
    private final MatchScoreRepository matchScoreRepository;
    private final ResumeRepository resumeRepository;
    private final AiUsageLogRepository aiUsageLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async("aiExecutor")
    @EventListener
    @Transactional
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        Application app = event.application();
        log.info("Async AI scoring triggered for application ID: {}", app.getId());

        try {
            Job job = app.getJob();
            Resume resume = app.getResume();

            String resumeText = "";
            if (resume != null && resume.getRawText() != null) {
                resumeText = resume.getRawText();
            } else if (resume != null) {
                Optional<Resume> freshResume = resumeRepository.findById(resume.getId());
                if (freshResume.isPresent() && freshResume.get().getRawText() != null) {
                    resumeText = freshResume.get().getRawText();
                }
            }

            List<String> requiredSkills = job.getJobSkills().stream()
                    .map(js -> js.getSkill().getName())
                    .toList();

            MatchScoreResult result = aiGateway.computeMatchScore(
                    resumeText, job.getTitle(), job.getDescription(), requiredSkills);

            MatchScore matchScore = MatchScore.builder()
                    .application(app)
                    .resume(resume)
                    .score((short) result.getScore())
                    .matchedSkills(objectMapper.writeValueAsString(result.getMatchedSkills()))
                    .missingSkills(objectMapper.writeValueAsString(result.getMissingSkills()))
                    .experienceGap(result.getExperienceGap())
                    .aiSummary(result.getAiSummary())
                    .interviewQuestions(objectMapper.writeValueAsString(result.getInterviewQuestions()))
                    .modelUsed(result.getModelUsed())
                    .promptTokens(result.getPromptTokens())
                    .completionTokens(result.getCompletionTokens())
                    .build();

            matchScoreRepository.save(matchScore);
            log.info("Saved MatchScore for application ID: {}, score: {}", app.getId(), result.getScore());

            // Track token usage and estimated cost
            AiUsageLog usageLog = AiUsageLog.builder()
                    .referenceId(app.getId())
                    .referenceType("MATCH_SCORE")
                    .model(result.getModelUsed())
                    .promptTokens(result.getPromptTokens())
                    .completionTokens(result.getCompletionTokens())
                    .estimatedCostUsd(BigDecimal.valueOf(0.0015)) // gpt-4o-mini average cost
                    .build();

            aiUsageLogRepository.save(usageLog);

        } catch (Exception e) {
            log.error("Failed to compute AI match score for application ID: {}", app.getId(), e);
        }
    }
}
