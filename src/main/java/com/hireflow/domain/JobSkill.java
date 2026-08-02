package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity for the {@code job_skills} junction table.
 * Maps the many-to-many relationship between {@link Job} and {@link Skill}
 * with the additional {@code is_required} attribute.
 */
@Entity
@Table(name = "job_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(JobSkill.JobSkillId.class)
public class JobSkill {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private boolean isRequired = true;

    /** Composite primary key class. */
    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobSkillId implements Serializable {
        private Job job;
        private Skill skill;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JobSkillId that)) return false;
            return Objects.equals(job, that.job) && Objects.equals(skill, that.skill);
        }

        @Override
        public int hashCode() {
            return Objects.hash(job, skill);
        }
    }
}
