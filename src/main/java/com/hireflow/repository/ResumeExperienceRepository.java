package com.hireflow.repository;

import com.hireflow.domain.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, Integer> {

    List<ResumeExperience> findByResumeId(UUID resumeId);

    void deleteByResumeId(UUID resumeId);
}
