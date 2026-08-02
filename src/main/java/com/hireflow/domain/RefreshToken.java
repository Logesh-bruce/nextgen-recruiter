package com.hireflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code refresh_tokens} table.
 *
 * <p>Implements refresh-token rotation with family-based replay detection:
 * if a revoked token in a family is used, the entire family is invalidated.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Opaque random token stored and compared on refresh. */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    /** Groups tokens in a rotation chain — replay of any revoked token revokes all. */
    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private boolean isRevoked = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
