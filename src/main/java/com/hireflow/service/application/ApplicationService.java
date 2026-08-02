package com.hireflow.service.application;

import com.hireflow.domain.enums.ApplicationStatus;
import com.hireflow.dto.request.CreateApplicationRequest;
import com.hireflow.dto.request.UpdateApplicationStatusRequest;
import com.hireflow.dto.response.ApplicationResponse;
import com.hireflow.dto.response.ApplicationSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract for job applications workflow.
 */
public interface ApplicationService {

    ApplicationResponse submitApplication(UUID candidateUserId, CreateApplicationRequest request);

    ApplicationResponse getApplicationById(UUID applicationId, UUID currentUserId);

    Page<ApplicationSummaryResponse> getMyApplications(UUID candidateUserId, ApplicationStatus status, Pageable pageable);

    Page<ApplicationSummaryResponse> getRecruiterApplications(UUID recruiterUserId, UUID jobId, ApplicationStatus status, Pageable pageable);

    ApplicationResponse updateApplicationStatus(UUID applicationId, UUID recruiterUserId, UpdateApplicationStatusRequest request);

    ApplicationResponse withdrawApplication(UUID applicationId, UUID candidateUserId);
}
