package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * JPA entity for the {@code resume_skills} junction table.
 */
@Entity
@Table(name = "resume_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ResumeSkill.ResumeSkillId.class)
public class ResumeSkill {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(precision = 4, scale = 3)
    private BigDecimal confidence;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeSkillId implements Serializable {
        private Resume resume;
        private Skill skill;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ResumeSkillId that)) return false;
            return Objects.equals(resume, that.resume) && Objects.equals(skill, that.skill);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resume, skill);
        }
    }
}
