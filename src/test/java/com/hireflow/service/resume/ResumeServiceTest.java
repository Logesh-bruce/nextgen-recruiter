package com.hireflow.service.resume;

import com.hireflow.domain.Candidate;
import com.hireflow.domain.Resume;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.response.ResumeUploadResponse;
import com.hireflow.repository.CandidateRepository;
import com.hireflow.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private ResumeParserService resumeParserService;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private User candidateUser;
    private Candidate candidate;
    private Resume resume;
    private UUID userId;
    private UUID candidateId;
    private UUID resumeId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        candidateId = UUID.randomUUID();
        resumeId = UUID.randomUUID();

        candidateUser = User.builder().id(userId).email("candidate@example.com").role(UserRole.CANDIDATE).build();
        candidate = Candidate.builder().id(candidateId).user(candidateUser).build();
        resume = Resume.builder().id(resumeId).candidate(candidate).fileName("cv.pdf").parseStatus("PENDING").build();
    }

    @Test
    @DisplayName("Should upload resume and trigger async parsing pipeline")
    void testUploadResume() {
        MockMultipartFile file = new MockMultipartFile("file", "cv.pdf", "application/pdf", "Dummy PDF content".getBytes());

        when(candidateRepository.findByUserId(userId)).thenReturn(Optional.of(candidate));
        when(resumeParserService.validateAndDetectMimeType(file)).thenReturn("application/pdf");
        when(fileStorageService.storeFile(eq(file), anyString())).thenReturn("resumes/cv.pdf");
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        ResumeUploadResponse response = resumeService.uploadResume(userId, file, true);

        assertNotNull(response);
        assertEquals(resumeId, response.getResumeId());
        verify(resumeParserService, times(1)).parseResumeAsync(any(Resume.class));
    }
}
