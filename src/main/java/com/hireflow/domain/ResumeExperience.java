package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * JPA entity for the {@code resume_experiences} table.
 * One work experience entry parsed from a resume.
 */
@Entity
@Table(name = "resume_experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "job_title", length = 255)
    private String jobTitle;

    @Column(length = 255)
    private String company;

    @Column(length = 255)
    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    /** Null = current role. */
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;
}
