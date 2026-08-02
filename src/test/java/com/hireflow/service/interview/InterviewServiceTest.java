package com.hireflow.service.interview;

import com.hireflow.domain.*;
import com.hireflow.domain.enums.InterviewStatus;
import com.hireflow.domain.enums.InterviewType;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.CreateInterviewRequest;
import com.hireflow.dto.response.InterviewResponse;
import com.hireflow.event.InterviewScheduledEvent;
import com.hireflow.mapper.InterviewMapper;
import com.hireflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private InterviewQuestionRepository questionRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private MatchScoreRepository matchScoreRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private User recruiterUser;
    private Recruiter recruiter;
    private Job job;
    private Application application;
    private Interview interview;
    private UUID recruiterUserId;
    private UUID appId;
    private UUID interviewId;

    @BeforeEach
    void setUp() {
        recruiterUserId = UUID.randomUUID();
        appId = UUID.randomUUID();
        interviewId = UUID.randomUUID();

        recruiterUser = User.builder().id(recruiterUserId).email("recruiter@techcorp.io").role(UserRole.RECRUITER).build();
        recruiter = Recruiter.builder().id(UUID.randomUUID()).user(recruiterUser).companyName("TechCorp").build();
        job = Job.builder().id(UUID.randomUUID()).title("DevOps Engineer").recruiter(recruiter).build();
        application = Application.builder().id(appId).job(job).build();
        interview = Interview.builder().id(interviewId).application(application).job(job).status(InterviewStatus.SCHEDULED).build();
    }

    @Test
    @DisplayName("Should schedule interview and publish calendar event")
    void testScheduleInterview() {
        CreateInterviewRequest request = new CreateInterviewRequest();
        request.setApplicationId(appId);
        request.setInterviewType(InterviewType.VIDEO);
        request.setScheduledAt(Instant.now().plus(2, ChronoUnit.DAYS));

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));
        when(userRepository.findById(recruiterUserId)).thenReturn(Optional.of(recruiterUser));
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);
        when(interviewMapper.toInterviewResponse(interview)).thenReturn(InterviewResponse.builder().id(interviewId).build());

        InterviewResponse response = interviewService.scheduleInterview(recruiterUserId, request);

        assertNotNull(response);
        assertEquals(interviewId, response.getId());
        verify(interviewRepository, times(1)).save(any(Interview.class));
        verify(eventPublisher, times(1)).publishEvent(any(InterviewScheduledEvent.class));
    }
}
