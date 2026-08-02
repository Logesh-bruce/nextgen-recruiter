package com.hireflow.repository;

import com.hireflow.domain.Application;
import com.hireflow.domain.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Application} entity.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    Optional<Application> findByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    Page<Application> findByCandidateId(UUID candidateId, Pageable pageable);

    Page<Application> findByCandidateIdAndStatus(UUID candidateId, ApplicationStatus status, Pageable pageable);

    Page<Application> findByJobId(UUID jobId, Pageable pageable);

    Page<Application> findByJobIdAndStatus(UUID jobId, ApplicationStatus status, Pageable pageable);

    @Query("SELECT a FROM Application a JOIN Recruiter r ON a.job.recruiter.id = r.id " +
            "WHERE r.id = :recruiterId " +
            "AND (:jobId IS NULL OR a.job.id = :jobId) " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<Application> findRecruiterApplications(
            @Param("recruiterId") UUID recruiterId,
            @Param("jobId") UUID jobId,
            @Param("status") ApplicationStatus status,
            Pageable pageable);
}
