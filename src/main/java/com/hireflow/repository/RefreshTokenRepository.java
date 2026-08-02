package com.hireflow.repository;

import com.hireflow.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link RefreshToken} entity.
 * Supports refresh-token rotation and token-family revocation.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    /** Returns all tokens in the same rotation family (for replay-attack revocation). */
    List<RefreshToken> findByFamilyId(UUID familyId);

    /** Revoke all tokens belonging to a user (used on logout-all / account deactivation). */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
    int revokeAllByUserId(UUID userId);

    /** Revoke all tokens in a family (called when a revoked token is replayed). */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.familyId = :familyId")
    int revokeAllByFamilyId(UUID familyId);

    /** Housekeeping — delete expired tokens to keep the table small. */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(Instant now);
}
