package com.hireflow.repository;

import com.hireflow.domain.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, ResumeSkill.ResumeSkillId> {

    List<ResumeSkill> findByResumeId(UUID resumeId);

    void deleteByResumeId(UUID resumeId);
}
