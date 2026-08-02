package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity for the {@code skills} master table.
 * Shared lookup table — referenced by both {@code job_skills} and {@code resume_skills}.
 */
@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** e.g., "programming", "soft", "tool", "framework" */
    @Column(length = 50)
    private String category;
}
