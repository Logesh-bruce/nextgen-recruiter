package com.hireflow.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireflow.domain.Application;
import com.hireflow.domain.Job;
import com.hireflow.domain.MatchScore;
import com.hireflow.domain.Resume;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.GenerateQuestionsRequest;
import com.hireflow.dto.response.*;
import com.hireflow.event.ApplicationSubmittedEvent;
import com.hireflow.exception.AccessDeniedException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.mapper.ApplicationMapper;
import com.hireflow.repository.ApplicationRepository;
import com.hireflow.repository.JobRepository;
import com.hireflow.repository.MatchScoreRepository;
import com.hireflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link AiService}.
 * Handles application scoring, Redis caching, question generation, and applicant rankings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final MatchScoreRepository matchScoreRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AiGateway aiGateway;
    private final ApplicationMapper applicationMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Cacheable(value = "matchScores", key = "#applicationId")
    @Transactional(readOnly = true)
    public MatchScoreResponse getApplicationScore(UUID applicationId, UUID currentUserId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Application", "id", applicationId));

        verifyAccessPermission(app, currentUserId);

        MatchScore matchScore = matchScoreRepository.findByApplicationId(applicationId)
                .orElseGet(() -> computeAndSaveScoreDirectly(app));

        return applicationMapper.toMatchScoreResponse(matchScore);
    }

    @Override
    @CacheEvict(value = "matchScores", key = "#applicationId")
    @Transactional
    public void refreshApplicationScore(UUID applicationId, UUID recruiterUserId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Application", "id", applicationId));

        if (!app.getJob().getRecruiter().getUser().getId().equals(recruiterUserId)) {
            throw new AccessDeniedException("You do not have permission to rescore this application");
        }

        matchScoreRepository.deleteByApplicationId(applicationId);
        computeAndSaveScoreDirectly(app);
        log.info("Refreshed AI match score for application ID: {}", applicationId);
    }

    @Override
    public GeneratedQuestionsResponse generateQuestions(GenerateQuestionsRequest request) {
        return aiGateway.generateInterviewQuestions(request.getJobTitle(), request.getSkills(), request.getCount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicantRankingResponse> getApplicantRanking(UUID jobId, UUID recruiterUserId, Integer minScore) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> ResourceNotFoundException.of("Job", "id", jobId));

        if (!job.getRecruiter().getUser().getId().equals(recruiterUserId)) {
            throw new AccessDeniedException("You do not have permission to view rankings for this job");
        }

        var applications = applicationRepository.findByJobId(jobId, PageRequest.of(0, 100)).getContent();

        List<ApplicantRankingResponse> rankings = new ArrayList<>();
        AtomicInteger rankCounter = new AtomicInteger(1);

        for (Application app : applications) {
            matchScoreRepository.findByApplicationId(app.getId()).ifPresent(ms -> {
                int score = ms.getScore();
                if (minScore == null || score >= minScore) {
                    rankings.add(ApplicantRankingResponse.builder()
                            .rank(rankCounter.getAndIncrement())
                            .applicationId(app.getId())
                            .candidateName(app.getCandidate().getUser().getFirstName() + " " + app.getCandidate().getUser().getLastName())
                            .matchScore(score)
                            .matchedSkills(parseJsonList(ms.getMatchedSkills()))
                            .missingSkills(parseJsonList(ms.getMissingSkills()))
                            .build());
                }
            });
        }

        rankings.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    private MatchScore computeAndSaveScoreDirectly(Application app) {
        Job job = app.getJob();
        Resume resume = app.getResume();
        String resumeText = resume != null && resume.getRawText() != null ? resume.getRawText() : "";

        List<String> requiredSkills = job.getJobSkills().stream()
                .map(js -> js.getSkill().getName())
                .toList();

        MatchScoreResult result = aiGateway.computeMatchScore(resumeText, job.getTitle(), job.getDescription(), requiredSkills);

        try {
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

            return matchScoreRepository.save(matchScore);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save match score", e);
        }
    }

    private void verifyAccessPermission(Application app, UUID userId) {
        var currentUser = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", userId));

        boolean isCandidate = app.getCandidate().getUser().getId().equals(userId);
        boolean isRecruiter = app.getJob().getRecruiter().getUser().getId().equals(userId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isCandidate && !isRecruiter && !isAdmin) {
            throw new AccessDeniedException("Access denied to application match score");
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
