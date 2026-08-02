package com.hireflow.repository;

import com.hireflow.domain.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkill.JobSkillId> {

    List<JobSkill> findByJobId(UUID jobId);

    void deleteByJobId(UUID jobId);
}
