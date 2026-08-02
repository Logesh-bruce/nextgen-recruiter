package com.hireflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * HireFlow AI — Backend Entry Point
 *
 * <p>Spring Boot 3.x application providing REST APIs for the AI-powered recruitment platform.
 * Covers: Job management, application tracking, resume parsing, AI match scoring, interviews,
 * and event-driven notifications.
 *
 * <p>Profiles:
 * <ul>
 *   <li>{@code local}  — local development with docker-compose</li>
 *   <li>{@code dev}    — shared dev/staging environment</li>
 *   <li>{@code prod}   — production (AWS ECS + RDS)</li>
 * </ul>
 */
@SpringBootApplication
@EnableAsync          // Required for async notification listeners
@EnableCaching        // Required for Redis match-score + search caching
public class HireFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(HireFlowApplication.class, args);
    }
}
