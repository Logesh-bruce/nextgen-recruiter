package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * JPA entity for the {@code resume_educations} table.
 * One education entry parsed from a resume.
 */
@Entity
@Table(name = "resume_educations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(length = 255)
    private String degree;

    @Column(name = "field_of_study", length = 255)
    private String fieldOfStudy;

    @Column(length = 255)
    private String institution;

    @Column(name = "graduation_year")
    private Short graduationYear;

    @Column(precision = 4, scale = 2)
    private BigDecimal gpa;
}
