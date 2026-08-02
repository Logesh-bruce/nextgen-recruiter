package com.hireflow.repository;

import com.hireflow.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Resume} entity.
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    List<Resume> findByCandidateIdOrderByCreatedAtDesc(UUID candidateId);

    Optional<Resume> findByCandidateIdAndIsPrimaryTrue(UUID candidateId);

    @Modifying
    @Query("UPDATE Resume r SET r.isPrimary = false WHERE r.candidate.id = :candidateId")
    void resetPrimaryResumesForCandidate(@Param("candidateId") UUID candidateId);
}
