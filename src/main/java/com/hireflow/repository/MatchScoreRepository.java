package com.hireflow.repository;

import com.hireflow.domain.MatchScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link MatchScore} entity.
 */
@Repository
public interface MatchScoreRepository extends JpaRepository<MatchScore, UUID> {

    Optional<MatchScore> findByApplicationId(UUID applicationId);

    boolean existsByApplicationId(UUID applicationId);

    void deleteByApplicationId(UUID applicationId);
}
