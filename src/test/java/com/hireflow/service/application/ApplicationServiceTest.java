package com.hireflow.service.application;

import com.hireflow.domain.Application;
import com.hireflow.domain.Candidate;
import com.hireflow.domain.Job;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.ApplicationStatus;
import com.hireflow.domain.enums.JobStatus;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.CreateApplicationRequest;
import com.hireflow.dto.response.ApplicationResponse;
import com.hireflow.event.ApplicationSubmittedEvent;
import com.hireflow.mapper.ApplicationMapper;
import com.hireflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private RecruiterRepository recruiterRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private MatchScoreRepository matchScoreRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private User candidateUser;
    private Candidate candidate;
    private Job job;
    private Application application;
    private UUID userId;
    private UUID candidateId;
    private UUID jobId;
    private UUID appId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        candidateId = UUID.randomUUID();
        jobId = UUID.randomUUID();
        appId = UUID.randomUUID();

        candidateUser = User.builder().id(userId).email("candidate@example.com").role(UserRole.CANDIDATE).build();
        candidate = Candidate.builder().id(candidateId).user(candidateUser).build();
        job = Job.builder().id(jobId).title("Backend Engineer").status(JobStatus.ACTIVE).build();
        application = Application.builder().id(appId).job(job).candidate(candidate).status(ApplicationStatus.APPLIED).build();
    }

    @Test
    @DisplayName("Should submit application successfully and publish event")
    void testSubmitApplication() {
        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setJobId(jobId);

        when(candidateRepository.findByUserId(userId)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);
        when(applicationMapper.toApplicationResponse(application)).thenReturn(ApplicationResponse.builder().id(appId).jobId(jobId).build());

        ApplicationResponse response = applicationService.submitApplication(userId, request);

        assertNotNull(response);
        assertEquals(appId, response.getId());
        verify(applicationRepository, times(1)).save(any(Application.class));
        verify(eventPublisher, times(1)).publishEvent(any(ApplicationSubmittedEvent.class));
    }
}
