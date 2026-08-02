package com.hireflow.service.job;

import com.hireflow.domain.enums.JobStatus;
import com.hireflow.dto.request.CreateJobRequest;
import com.hireflow.dto.request.UpdateJobStatusRequest;
import com.hireflow.dto.response.JobResponse;
import com.hireflow.dto.response.JobSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract for job posting operations.
 */
public interface JobService {

    Page<JobSummaryResponse> getActiveJobs(String search, String location, Boolean isRemote, Pageable pageable);

    JobResponse getJobById(UUID id, UUID currentUserId);

    JobResponse createJob(UUID recruiterUserId, CreateJobRequest request);

    JobResponse updateJob(UUID jobId, UUID recruiterUserId, CreateJobRequest request);

    JobResponse updateJobStatus(UUID jobId, UUID currentUserId, UpdateJobStatusRequest request);

    void deleteJob(UUID jobId, UUID currentUserId);

    Page<JobSummaryResponse> getMyJobs(UUID recruiterUserId, JobStatus status, Pageable pageable);
}
