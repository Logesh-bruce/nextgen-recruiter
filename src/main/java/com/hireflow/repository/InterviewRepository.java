package com.hireflow.repository;

import com.hireflow.domain.Interview;
import com.hireflow.domain.enums.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Interview} entity.
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    Page<Interview> findByApplicationId(UUID applicationId, Pageable pageable);

    Page<Interview> findByJobId(UUID jobId, Pageable pageable);

    Page<Interview> findByStatus(InterviewStatus status, Pageable pageable);

    List<Interview> findByApplicationCandidateUserId(UUID candidateUserId);

    List<Interview> findByJobRecruiterUserId(UUID recruiterUserId);
}
