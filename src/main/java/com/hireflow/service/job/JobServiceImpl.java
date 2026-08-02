package com.hireflow.service.job;

import com.hireflow.domain.Job;
import com.hireflow.domain.JobSkill;
import com.hireflow.domain.Recruiter;
import com.hireflow.domain.Skill;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.JobStatus;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.CreateJobRequest;
import com.hireflow.dto.request.JobSkillRequest;
import com.hireflow.dto.request.UpdateJobStatusRequest;
import com.hireflow.dto.response.JobResponse;
import com.hireflow.dto.response.JobSummaryResponse;
import com.hireflow.exception.AccessDeniedException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.mapper.JobMapper;
import com.hireflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link JobService}.
 * Handles job postings, search, skills management, and recruiter access controls.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final RecruiterRepository recruiterRepository;
    private final SkillRepository skillRepository;
    private final JobSkillRepository jobSkillRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> getActiveJobs(String search, String location, Boolean isRemote, Pageable pageable) {
        return jobRepository.searchActiveJobs(search, location, isRemote, pageable)
                .map(jobMapper::toJobSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(UUID id, UUID currentUserId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Job", "id", id));

        if (job.getStatus() != JobStatus.ACTIVE) {
            if (currentUserId == null) {
                throw ResourceNotFoundException.of("Job", "id", id);
            }
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> ResourceNotFoundException.of("User", "id", currentUserId));

            boolean isOwner = job.getRecruiter().getUser().getId().equals(currentUserId);
            boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

            if (!isOwner && !isAdmin) {
                throw ResourceNotFoundException.of("Job", "id", id);
            }
        }

        return jobMapper.toJobResponse(job);
    }

    @Override
    @Transactional
    public JobResponse createJob(UUID recruiterUserId, CreateJobRequest request) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for user: " + recruiterUserId));

        Job job = jobMapper.toJob(request);
        job.setRecruiter(recruiter);

        Job savedJob = jobRepository.save(job);

        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            attachSkillsToJob(savedJob, request.getSkills());
        }

        log.info("Job created: id={}, title={}, recruiterId={}", savedJob.getId(), savedJob.getTitle(), recruiter.getId());
        return jobMapper.toJobResponse(jobRepository.findById(savedJob.getId()).orElse(savedJob));
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID jobId, UUID recruiterUserId, CreateJobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> ResourceNotFoundException.of("Job", "id", jobId));

        verifyRecruiterOwnership(job, recruiterUserId);

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setRemote(request.isRemote());
        job.setJobType(request.getJobType());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setCurrency(request.getCurrency());
        job.setApplicationDeadline(request.getApplicationDeadline());

        jobSkillRepository.deleteByJobId(jobId);
        job.getJobSkills().clear();

        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            attachSkillsToJob(job, request.getSkills());
        }

        Job updatedJob = jobRepository.save(job);
        log.info("Job updated: id={}", updatedJob.getId());

        return jobMapper.toJobResponse(updatedJob);
    }

    @Override
    @Transactional
    public JobResponse updateJobStatus(UUID jobId, UUID currentUserId, UpdateJobStatusRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> ResourceNotFoundException.of("Job", "id", jobId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", currentUserId));

        boolean isOwner = job.getRecruiter().getUser().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to modify this job status");
        }

        JobStatus oldStatus = job.getStatus();
        JobStatus newStatus = request.getStatus();

        if (oldStatus != newStatus) {
            job.setStatus(newStatus);
            if (newStatus == JobStatus.ACTIVE && job.getPublishedAt() == null) {
                job.setPublishedAt(Instant.now());
            }
            jobRepository.save(job);
            log.info("Job status changed: id={}, old={}, new={}", jobId, oldStatus, newStatus);
        }

        return jobMapper.toJobResponse(job);
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId, UUID currentUserId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> ResourceNotFoundException.of("Job", "id", jobId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", currentUserId));

        boolean isOwner = job.getRecruiter().getUser().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this job");
        }

        jobRepository.delete(job);
        log.info("Job deleted: id={}", jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> getMyJobs(UUID recruiterUserId, JobStatus status, Pageable pageable) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for user: " + recruiterUserId));

        if (status != null) {
            return jobRepository.findByRecruiterIdAndStatus(recruiter.getId(), status, pageable)
                    .map(jobMapper::toJobSummaryResponse);
        } else {
            return jobRepository.findByRecruiterId(recruiter.getId(), pageable)
                    .map(jobMapper::toJobSummaryResponse);
        }
    }

    private void verifyRecruiterOwnership(Job job, UUID userId) {
        if (!job.getRecruiter().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this job posting");
        }
    }

    private void attachSkillsToJob(Job job, List<JobSkillRequest> skillRequests) {
        List<JobSkill> jobSkills = new ArrayList<>();

        for (JobSkillRequest skillReq : skillRequests) {
            String skillName = skillReq.getName().trim();
            Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

            JobSkill jobSkill = JobSkill.builder()
                    .job(job)
                    .skill(skill)
                    .isRequired(skillReq.isRequired())
                    .build();

            jobSkills.add(jobSkill);
        }

        job.setJobSkills(jobSkills);
    }
}
