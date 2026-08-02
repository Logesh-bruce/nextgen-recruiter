package com.hireflow.service.interview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireflow.domain.*;
import com.hireflow.domain.enums.InterviewStatus;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.CreateInterviewRequest;
import com.hireflow.dto.request.UpdateInterviewRequest;
import com.hireflow.dto.response.InterviewQuestionResponse;
import com.hireflow.dto.response.InterviewResponse;
import com.hireflow.dto.response.MatchScoreResult;
import com.hireflow.event.InterviewScheduledEvent;
import com.hireflow.exception.AccessDeniedException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.mapper.InterviewMapper;
import com.hireflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link InterviewService}.
 * Handles interview scheduling, notification triggers, and AI question integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository questionRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchScoreRepository matchScoreRepository;
    private final UserRepository userRepository;
    private final InterviewMapper interviewMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public InterviewResponse scheduleInterview(UUID recruiterUserId, CreateInterviewRequest request) {
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> ResourceNotFoundException.of("Application", "id", request.getApplicationId()));

        Job job = application.getJob();
        if (!job.getRecruiter().getUser().getId().equals(recruiterUserId)) {
            throw new AccessDeniedException("You do not have permission to schedule interviews for this job");
        }

        User interviewer = userRepository.findById(recruiterUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", recruiterUserId));

        Interview interview = Interview.builder()
                .application(application)
                .job(job)
                .interviewer(interviewer)
                .interviewType(request.getInterviewType())
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .meetingLink(request.getMeetingLink())
                .locationNotes(request.getLocationNotes())
                .build();

        Interview savedInterview = interviewRepository.save(interview);
        log.info("Scheduled interview ID: {} for application ID: {}", savedInterview.getId(), application.getId());

        // Attach AI questions from MatchScore
        attachAiQuestionsToInterview(savedInterview, application.getId());

        // Publish event for email/SMS calendar notification
        eventPublisher.publishEvent(new InterviewScheduledEvent(savedInterview));

        return interviewMapper.toInterviewResponse(savedInterview);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(UUID interviewId, UUID currentUserId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> ResourceNotFoundException.of("Interview", "id", interviewId));

        verifyAccess(interview, currentUserId);
        return interviewMapper.toInterviewResponse(interview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewResponse> getInterviews(UUID currentUserId, UUID applicationId, InterviewStatus status, Pageable pageable) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", currentUserId));

        if (applicationId != null) {
            return interviewRepository.findByApplicationId(applicationId, pageable).map(interviewMapper::toInterviewResponse);
        }

        if (user.getRole() == UserRole.RECRUITER) {
            return interviewRepository.findByJobRecruiterUserId(currentUserId)
                    .stream()
                    .filter(i -> status == null || i.getStatus() == status)
                    .map(interviewMapper::toInterviewResponse)
                    .collect(org.springframework.data.domain.PageImpl::new, (page, item) -> {}, (page1, page2) -> {});
        } else {
            return interviewRepository.findByApplicationCandidateUserId(currentUserId)
                    .stream()
                    .filter(i -> status == null || i.getStatus() == status)
                    .map(interviewMapper::toInterviewResponse)
                    .collect(org.springframework.data.domain.PageImpl::new, (page, item) -> {}, (page1, page2) -> {});
        }
    }

    @Override
    @Transactional
    public InterviewResponse updateInterview(UUID interviewId, UUID recruiterUserId, UpdateInterviewRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> ResourceNotFoundException.of("Interview", "id", interviewId));

        if (!interview.getJob().getRecruiter().getUser().getId().equals(recruiterUserId)) {
            throw new AccessDeniedException("You do not have permission to update this interview");
        }

        if (request.getScheduledAt() != null) {
            interview.setScheduledAt(request.getScheduledAt());
        }
        if (request.getDurationMinutes() != null) {
            interview.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getMeetingLink() != null) {
            interview.setMeetingLink(request.getMeetingLink());
        }
        if (request.getLocationNotes() != null) {
            interview.setLocationNotes(request.getLocationNotes());
        }
        if (request.getStatus() != null) {
            interview.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            interview.setNotes(request.getNotes());
        }

        Interview updated = interviewRepository.save(interview);
        log.info("Updated interview ID: {}", interviewId);

        return interviewMapper.toInterviewResponse(updated);
    }

    @Override
    @Transactional
    public void cancelInterview(UUID interviewId, UUID recruiterUserId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> ResourceNotFoundException.of("Interview", "id", interviewId));

        if (!interview.getJob().getRecruiter().getUser().getId().equals(recruiterUserId)) {
            throw new AccessDeniedException("You do not have permission to cancel this interview");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepository.save(interview);
        log.info("Cancelled interview ID: {}", interviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewQuestionResponse> getInterviewQuestions(UUID interviewId, UUID recruiterUserId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> ResourceNotFoundException.of("Interview", "id", interviewId));

        if (!interview.getJob().getRecruiter().getUser().getId().equals(recruiterUserId)) {
            throw new AccessDeniedException("Only recruiters can view interview questions");
        }

        return questionRepository.findByInterviewIdOrderBySortOrderAsc(interviewId)
                .stream()
                .map(interviewMapper::toQuestionResponse)
                .toList();
    }

    private void attachAiQuestionsToInterview(Interview interview, UUID applicationId) {
        matchScoreRepository.findByApplicationId(applicationId).ifPresent(ms -> {
            try {
                if (ms.getInterviewQuestions() != null) {
                    List<MatchScoreResult.QuestionItem> questions = objectMapper.readValue(
                            ms.getInterviewQuestions(), new TypeReference<>() {});

                    short order = 1;
                    List<InterviewQuestion> entityQuestions = new ArrayList<>();
                    for (MatchScoreResult.QuestionItem q : questions) {
                        entityQuestions.add(InterviewQuestion.builder()
                                .interview(interview)
                                .question(q.getQuestion())
                                .category(q.getCategory())
                                .sortOrder(order++)
                                .build());
                    }
                    questionRepository.saveAll(entityQuestions);
                }
            } catch (Exception e) {
                log.warn("Could not parse AI questions for interview ID: {}", interview.getId(), e);
            }
        });
    }

    private void verifyAccess(Interview interview, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", userId));

        boolean isCandidate = interview.getApplication().getCandidate().getUser().getId().equals(userId);
        boolean isRecruiter = interview.getJob().getRecruiter().getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isCandidate && !isRecruiter && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to view this interview");
        }
    }
}
