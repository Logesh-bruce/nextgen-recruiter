package com.hireflow.repository;

import com.hireflow.domain.Job;
import com.hireflow.domain.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link Job} entity with full-text search and custom filtering capabilities.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    Page<Job> findByRecruiterId(UUID recruiterId, Pageable pageable);

    Page<Job> findByRecruiterIdAndStatus(UUID recruiterId, JobStatus status, Pageable pageable);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    /**
     * PostgreSQL Full-Text Search query across title and description using the fts_vector column.
     */
    @Query(value = "SELECT * FROM jobs j WHERE j.status = 'ACTIVE' " +
            "AND (:search IS NULL OR j.fts_vector @@ plainto_tsquery('english', :search)) " +
            "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:isRemote IS NULL OR j.is_remote = :isRemote)",
            countQuery = "SELECT count(*) FROM jobs j WHERE j.status = 'ACTIVE' " +
                    "AND (:search IS NULL OR j.fts_vector @@ plainto_tsquery('english', :search)) " +
                    "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
                    "AND (:isRemote IS NULL OR j.is_remote = :isRemote)",
            nativeQuery = true)
    Page<Job> searchActiveJobs(
            @Param("search") String search,
            @Param("location") String location,
            @Param("isRemote") Boolean isRemote,
            Pageable pageable);
}
