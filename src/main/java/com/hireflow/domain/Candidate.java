package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code candidates} table.
 * One-to-one with {@link User} (role = CANDIDATE).
 */
@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** e.g., "Senior Java Developer" */
    @Column(length = 255)
    private String headline;

    @Column(length = 255)
    private String location;

    @Column(name = "years_of_exp")
    private Short yearsOfExp;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "portfolio_url", length = 255)
    private String portfolioUrl;

    @Column(name = "is_open_to_work", nullable = false)
    @Builder.Default
    private boolean isOpenToWork = true;

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
