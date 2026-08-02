package com.hireflow;

import com.hireflow.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads without errors.
 * This catches misconfigured beans, missing properties, and circular dependencies early.
 */
@SpringBootTest
@ActiveProfiles("test")
class HireFlowApplicationTests {

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void contextLoads() {
        // If the context fails to start, this test will fail with a detailed error.
    }
}
