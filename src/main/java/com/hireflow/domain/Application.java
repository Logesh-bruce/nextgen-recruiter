package com.hireflow.domain;

import com.hireflow.domain.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code applications} table.
 *
 * <p>Unique constraint {@code (job_id, candidate_id)} enforces one application
 * per candidate per job at the database level.
 */
@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /** The specific resume version submitted with this application. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "app_status")
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "status_updated_at", nullable = false)
    @Builder.Default
    private Instant statusUpdatedAt = Instant.now();

    /** Internal recruiter notes — not visible to the candidate. */
    @Column(name = "recruiter_notes", columnDefinition = "TEXT")
    private String recruiterNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
