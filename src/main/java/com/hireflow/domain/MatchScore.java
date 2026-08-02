package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the {@code match_scores} table.
 *
 * <p>Stores AI-generated scoring output for a single {@link Application}.
 * JSONB columns ({@code matchedSkills}, {@code missingSkills}, {@code interviewQuestions})
 * are mapped using Hypersistence Utils {@code JsonBinaryType}.
 *
 * <p><b>Dependency note</b>: Add to pom.xml when this entity is used:
 * <pre>
 *   &lt;dependency&gt;
 *     &lt;groupId&gt;io.hypersistence&lt;/groupId&gt;
 *     &lt;artifactId&gt;hypersistence-utils-hibernate-63&lt;/artifactId&gt;
 *     &lt;version&gt;3.7.7&lt;/version&gt;
 *   &lt;/dependency&gt;
 * </pre>
 * Alternatively, use {@code @Column(columnDefinition = "jsonb")} with
 * a String field and manually serialize/deserialize with Jackson.
 */
@Entity
@Table(name = "match_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchScore {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    /** AI-computed match percentage 0–100. */
    @Column(nullable = false)
    private Short score;

    /** Serialised as JSONB — list of matched skill names. */
    @Column(name = "matched_skills", columnDefinition = "jsonb")
    private String matchedSkills;

    /** Serialised as JSONB — list of missing skill names. */
    @Column(name = "missing_skills", columnDefinition = "jsonb")
    private String missingSkills;

    @Column(name = "experience_gap", columnDefinition = "TEXT")
    private String experienceGap;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    /** Serialised as JSONB — [{question, category}] */
    @Column(name = "interview_questions", columnDefinition = "jsonb")
    private String interviewQuestions;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
