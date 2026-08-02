package com.hireflow.repository;

import com.hireflow.domain.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, Integer> {

    List<ResumeEducation> findByResumeId(UUID resumeId);

    void deleteByResumeId(UUID resumeId);
}
