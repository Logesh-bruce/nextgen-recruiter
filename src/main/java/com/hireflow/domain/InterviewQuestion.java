package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity for the {@code interview_questions} table.
 * AI-generated questions attached to an {@link Interview}.
 */
@Entity
@Table(name = "interview_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    /** "technical" | "behavioral" | "situational" */
    @Column(length = 50)
    private String category;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Short sortOrder = 0;
}
