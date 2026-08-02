package com.hireflow.service.resume;

import com.hireflow.dto.response.ParseStatusResponse;
import com.hireflow.dto.response.ResumeResponse;
import com.hireflow.dto.response.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for resume uploads, parsing, and management.
 */
public interface ResumeService {

    ResumeUploadResponse uploadResume(UUID candidateUserId, MultipartFile file, boolean isPrimary);

    List<ResumeResponse> getCandidateResumes(UUID candidateUserId);

    ResumeResponse getResumeById(UUID resumeId, UUID currentUserId);

    ParseStatusResponse getParseStatus(UUID resumeId, UUID candidateUserId);

    void deleteResume(UUID resumeId, UUID candidateUserId);
}
