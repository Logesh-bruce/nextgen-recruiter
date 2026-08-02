package com.hireflow.service.job;

import com.hireflow.domain.Job;
import com.hireflow.domain.Recruiter;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.JobStatus;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.CreateJobRequest;
import com.hireflow.dto.response.JobResponse;
import com.hireflow.mapper.JobMapper;
import com.hireflow.repository.JobRepository;
import com.hireflow.repository.JobSkillRepository;
import com.hireflow.repository.RecruiterRepository;
import com.hireflow.repository.SkillRepository;
import com.hireflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private RecruiterRepository recruiterRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private JobSkillRepository jobSkillRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private User recruiterUser;
    private Recruiter recruiter;
    private Job job;
    private UUID userId;
    private UUID recruiterId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        recruiterId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        recruiterUser = User.builder().id(userId).email("recruiter@techcorp.io").role(UserRole.RECRUITER).build();
        recruiter = Recruiter.builder().id(recruiterId).user(recruiterUser).companyName("TechCorp").build();
        job = Job.builder().id(jobId).title("Senior Java Engineer").recruiter(recruiter).status(JobStatus.DRAFT).build();
    }

    @Test
    @DisplayName("Should create job posting successfully")
    void testCreateJob() {
        CreateJobRequest request = new CreateJobRequest();
        request.setTitle("Senior Java Engineer");
        request.setDescription("Looking for Spring Boot expert");
        request.setJobType("FULL_TIME");

        when(recruiterRepository.findByUserId(userId)).thenReturn(Optional.of(recruiter));
        when(jobMapper.toJob(request)).thenReturn(job);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobMapper.toJobResponse(job)).thenReturn(JobResponse.builder().id(jobId).title("Senior Java Engineer").build());

        JobResponse response = jobService.createJob(userId, request);

        assertNotNull(response);
        assertEquals(jobId, response.getId());
        verify(jobRepository, times(1)).save(any(Job.class));
    }
}
