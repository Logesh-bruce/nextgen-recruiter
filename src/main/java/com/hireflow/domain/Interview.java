package com.hireflow.domain;

import com.hireflow.domain.enums.InterviewStatus;
import com.hireflow.domain.enums.InterviewType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the {@code interviews} table.
 */
@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /** The recruiter user conducting the interview. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id")
    private User interviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, columnDefinition = "interview_type")
    @Builder.Default
    private InterviewType interviewType = InterviewType.VIDEO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "interview_status")
    @Builder.Default
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Short durationMinutes = 60;

    @Column(name = "meeting_link", length = 512)
    private String meetingLink;

    @Column(name = "location_notes", columnDefinition = "TEXT")
    private String locationNotes;

    /** Post-interview notes written by the recruiter. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InterviewQuestion> questions = new ArrayList<>();

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
