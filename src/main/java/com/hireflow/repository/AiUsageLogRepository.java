package com.hireflow.repository;

import com.hireflow.domain.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Repository for {@link AiUsageLog} entity tracking token consumption and cost.
 */
@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {

    @Query("SELECT COALESCE(SUM(l.estimatedCostUsd), 0) FROM AiUsageLog l WHERE l.createdAt >= :since")
    BigDecimal getTotalCostSince(Instant since);

    @Query("SELECT COALESCE(SUM(l.promptTokens + l.completionTokens), 0) FROM AiUsageLog l WHERE l.createdAt >= :since")
    long getTotalTokensSince(Instant since);
}
