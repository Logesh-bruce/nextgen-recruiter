package com.hireflow.service.interview;

import com.hireflow.domain.enums.InterviewStatus;
import com.hireflow.dto.request.CreateInterviewRequest;
import com.hireflow.dto.request.UpdateInterviewRequest;
import com.hireflow.dto.response.InterviewQuestionResponse;
import com.hireflow.dto.response.InterviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for interview scheduling and questions management.
 */
public interface InterviewService {

    InterviewResponse scheduleInterview(UUID recruiterUserId, CreateInterviewRequest request);

    InterviewResponse getInterviewById(UUID interviewId, UUID currentUserId);

    Page<InterviewResponse> getInterviews(UUID currentUserId, UUID applicationId, InterviewStatus status, Pageable pageable);

    InterviewResponse updateInterview(UUID interviewId, UUID recruiterUserId, UpdateInterviewRequest request);

    void cancelInterview(UUID interviewId, UUID recruiterUserId);

    List<InterviewQuestionResponse> getInterviewQuestions(UUID interviewId, UUID recruiterUserId);
}
