package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code ai_usage_log} table.
 * Tracks token consumption and estimated cost per AI API call
 * for cost monitoring via the admin dashboard.
 */
@Entity
@Table(name = "ai_usage_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID of the related entity (e.g., application_id for match scoring). */
    @Column(name = "reference_id")
    private UUID referenceId;

    /** e.g., "MATCH_SCORE", "INTERVIEW_QS" */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private Integer promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private Integer completionTokens = 0;

    /**
     * Total tokens — computed column in DB (GENERATED ALWAYS AS).
     * Not insertable/updatable from JPA side.
     */
    @Column(name = "total_tokens", insertable = false, updatable = false)
    private Integer totalTokens;

    @Column(name = "estimated_cost_usd", precision = 10, scale = 6)
    private BigDecimal estimatedCostUsd;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
