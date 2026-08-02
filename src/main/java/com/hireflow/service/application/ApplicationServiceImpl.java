package com.hireflow.service.application;

import com.hireflow.domain.*;
import com.hireflow.domain.enums.ApplicationStatus;
import com.hireflow.domain.enums.JobStatus;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.CreateApplicationRequest;
import com.hireflow.dto.request.UpdateApplicationStatusRequest;
import com.hireflow.dto.response.ApplicationResponse;
import com.hireflow.dto.response.ApplicationSummaryResponse;
import com.hireflow.event.ApplicationStatusChangedEvent;
import com.hireflow.event.ApplicationSubmittedEvent;
import com.hireflow.exception.AccessDeniedException;
import com.hireflow.exception.BusinessRuleException;
import com.hireflow.exception.ConflictException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.mapper.ApplicationMapper;
import com.hireflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link ApplicationService}.
 * Manages candidate submissions, recruiter applicant reviews, status transitions, and events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;
    private final ResumeRepository resumeRepository;
    private final MatchScoreRepository matchScoreRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ApplicationResponse submitApplication(UUID candidateUserId, CreateApplicationRequest request) {
        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + candidateUserId));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> ResourceNotFoundException.of("Job", "id", request.getJobId()));

        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot apply to a job that is not ACTIVE");
        }

        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new ConflictException("You have already submitted an application for this job posting");
        }

        Resume resume = null;
        if (request.getResumeId() != null) {
            resume = resumeRepository.findById(request.getResumeId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Resume", "id", request.getResumeId()));

            if (!resume.getCandidate().getId().equals(candidate.getId())) {
                throw new AccessDeniedException("The specified resume does not belong to you");
            }
        } else {
            // Pick primary resume if not explicitly passed
            Optional<Resume> primaryResume = resumeRepository.findByCandidateIdAndIsPrimaryTrue(candidate.getId());
            if (primaryResume.isPresent()) {
                resume = primaryResume.get();
            }
        }

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resume(resume)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .build();

        Application savedApp = applicationRepository.save(application);
        log.info("Application submitted: id={}, candidateId={}, jobId={}", savedApp.getId(), candidate.getId(), job.getId());

        // Publish domain event for async AI match scoring & notification listeners
        eventPublisher.publishEvent(new ApplicationSubmittedEvent(savedApp));

        return buildApplicationResponse(savedApp);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID applicationId, UUID currentUserId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Application", "id", applicationId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", currentUserId));

        boolean isCandidateOwner = application.getCandidate().getUser().getId().equals(currentUserId);
        boolean isRecruiterOwner = application.getJob().getRecruiter().getUser().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isCandidateOwner && !isRecruiterOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to view this application");
        }

        return buildApplicationResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponse> getMyApplications(UUID candidateUserId, ApplicationStatus status, Pageable pageable) {
        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + candidateUserId));

        Page<Application> page = status != null ?
                applicationRepository.findByCandidateIdAndStatus(candidate.getId(), status, pageable) :
                applicationRepository.findByCandidateId(candidate.getId(), pageable);

        return page.map(this::buildApplicationSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponse> getRecruiterApplications(UUID recruiterUserId, UUID jobId, ApplicationStatus status, Pageable pageable) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for user: " + recruiterUserId));

        Page<Application> page = applicationRepository.findRecruiterApplications(recruiter.getId(), jobId, status, pageable);
        return page.map(this::buildApplicationSummaryResponse);
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, UUID recruiterUserId, UpdateApplicationStatusRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getUser().getId().equals(recruiterUserId)) {
            throw new AccessDeniedException("You do not have permission to update applications for this job posting");
        }

        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus newStatus = request.getStatus();

        application.setStatus(newStatus);
        application.setStatusUpdatedAt(Instant.now());
        if (request.getRecruiterNotes() != null) {
            application.setRecruiterNotes(request.getRecruiterNotes());
        }

        Application updatedApp = applicationRepository.save(application);
        log.info("Application status updated: id={}, old={}, new={}", applicationId, oldStatus, newStatus);

        if (oldStatus != newStatus) {
            eventPublisher.publishEvent(new ApplicationStatusChangedEvent(updatedApp, oldStatus));
        }

        return buildApplicationResponse(updatedApp);
    }

    @Override
    @Transactional
    public ApplicationResponse withdrawApplication(UUID applicationId, UUID candidateUserId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Application", "id", applicationId));

        if (!application.getCandidate().getUser().getId().equals(candidateUserId)) {
            throw new AccessDeniedException("You can only withdraw your own applications");
        }

        if (application.getStatus() == ApplicationStatus.REJECTED || application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BusinessRuleException("Application cannot be withdrawn in its current state: " + application.getStatus());
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setStatusUpdatedAt(Instant.now());

        Application updatedApp = applicationRepository.save(application);
        log.info("Application withdrawn by candidate: id={}", applicationId);

        eventPublisher.publishEvent(new ApplicationStatusChangedEvent(updatedApp, oldStatus));

        return buildApplicationResponse(updatedApp);
    }

    private ApplicationResponse buildApplicationResponse(Application application) {
        ApplicationResponse response = applicationMapper.toApplicationResponse(application);
        matchScoreRepository.findByApplicationId(application.getId())
                .ifPresent(ms -> response.setMatchScore(applicationMapper.toMatchScoreResponse(ms)));
        return response;
    }

    private ApplicationSummaryResponse buildApplicationSummaryResponse(Application application) {
        ApplicationSummaryResponse response = applicationMapper.toApplicationSummaryResponse(application);
        matchScoreRepository.findByApplicationId(application.getId())
                .ifPresent(ms -> response.setMatchScore((int) ms.getScore()));
        return response;
    }
}
