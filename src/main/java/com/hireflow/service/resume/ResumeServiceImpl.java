package com.hireflow.service.resume;

import com.hireflow.domain.Candidate;
import com.hireflow.domain.Resume;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.response.*;
import com.hireflow.exception.AccessDeniedException;
import com.hireflow.exception.BusinessRuleException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.mapper.ResumeMapper;
import com.hireflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link ResumeService}.
 * Manages resume uploads, async parsing dispatch, and retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeEducationRepository educationRepository;
    private final FileStorageService fileStorageService;
    private final ResumeParserService resumeParserService;
    private final ResumeMapper resumeMapper;

    @Override
    @Transactional
    public ResumeUploadResponse uploadResume(UUID candidateUserId, MultipartFile file, boolean isPrimary) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("Uploaded resume file cannot be empty");
        }

        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + candidateUserId));

        String detectedMimeType = resumeParserService.validateAndDetectMimeType(file);

        if (isPrimary) {
            resumeRepository.resetPrimaryResumesForCandidate(candidate.getId());
        }

        String storageKey = fileStorageService.storeFile(file, "resumes/" + candidate.getId());

        Resume resume = Resume.builder()
                .candidate(candidate)
                .fileName(file.getOriginalFilename())
                .s3Key(storageKey)
                .fileSizeBytes((int) file.getSize())
                .mimeType(detectedMimeType)
                .parseStatus("PENDING")
                .isPrimary(isPrimary)
                .build();

        Resume savedResume = resumeRepository.save(resume);
        log.info("Resume saved to DB with ID: {}", savedResume.getId());

        // Dispatch async background parsing pipeline
        resumeParserService.parseResumeAsync(savedResume);

        return ResumeUploadResponse.builder()
                .resumeId(savedResume.getId())
                .fileName(savedResume.getFileName())
                .parseStatus(savedResume.getParseStatus())
                .message("Resume uploaded successfully. Parsing in progress.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getCandidateResumes(UUID candidateUserId) {
        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + candidateUserId));

        return resumeRepository.findByCandidateIdOrderByCreatedAtDesc(candidate.getId())
                .stream()
                .map(this::buildResumeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(UUID resumeId, UUID currentUserId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Resume", "id", resumeId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", currentUserId));

        boolean isOwner = resume.getCandidate().getUser().getId().equals(currentUserId);
        boolean isRecruiterOrAdmin = currentUser.getRole() == UserRole.RECRUITER || currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isRecruiterOrAdmin) {
            throw new AccessDeniedException("You do not have permission to view this resume");
        }

        return buildResumeResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public ParseStatusResponse getParseStatus(UUID resumeId, UUID candidateUserId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Resume", "id", resumeId));

        if (!resume.getCandidate().getUser().getId().equals(candidateUserId)) {
            throw new AccessDeniedException("You do not own this resume");
        }

        return ParseStatusResponse.builder()
                .resumeId(resume.getId())
                .parseStatus(resume.getParseStatus())
                .build();
    }

    @Override
    @Transactional
    public void deleteResume(UUID resumeId, UUID candidateUserId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Resume", "id", resumeId));

        if (!resume.getCandidate().getUser().getId().equals(candidateUserId)) {
            throw new AccessDeniedException("You do not own this resume");
        }

        fileStorageService.deleteFile(resume.getS3Key());
        resumeRepository.delete(resume);
        log.info("Deleted resumeId: {}", resumeId);
    }

    private ResumeResponse buildResumeResponse(Resume resume) {
        ResumeResponse response = resumeMapper.toResumeResponse(resume);

        var skills = resumeSkillRepository.findByResumeId(resume.getId());
        response.setSkills(resumeMapper.mapResumeSkills(skills));

        var experiences = experienceRepository.findByResumeId(resume.getId());
        response.setExperiences(experiences.stream().map(resumeMapper::toExperienceResponse).toList());

        var educations = educationRepository.findByResumeId(resume.getId());
        response.setEducations(educations.stream().map(resumeMapper::toEducationResponse).toList());

        return response;
    }
}
