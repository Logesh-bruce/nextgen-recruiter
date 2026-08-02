package com.hireflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async thread-pool configuration.
 *
 * <p>Used by:
 * <ul>
 *   <li>Notification event listeners ({@code @Async} on {@code @EventListener})</li>
 *   <li>Resume parsing pipeline (background after file upload)</li>
 *   <li>AI scoring pipeline (triggered on application submission)</li>
 * </ul>
 *
 * <p>Pool is sized conservatively for a small-team SaaS project on ECS Fargate (0.25–0.5 vCPU).
 * Tune via env vars when scaling.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Default task executor used by all {@code @Async} methods
     * unless a specific executor bean name is referenced.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("hireflow-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for AI calls — longer timeout tolerance,
     * isolated from notification threads so a slow OpenAI response
     * doesn't starve email delivery.
     */
    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("hireflow-ai-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
