package com.hireflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads without errors.
 * This catches misconfigured beans, missing properties, and circular dependencies early.
 */
@SpringBootTest
@ActiveProfiles("test")
class HireFlowApplicationTests {

    @Test
    void contextLoads() {
        // If the context fails to start, this test will fail with a detailed error.
    }
}
