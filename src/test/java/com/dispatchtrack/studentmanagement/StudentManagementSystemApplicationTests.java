package com.dispatchtrack.studentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies the full Spring application context (controller,
 * service, repository, JPA, Actuator, etc.) wires up and starts correctly
 * against the in-memory H2 database configured for tests.
 */
@SpringBootTest
class StudentManagementSystemApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context fails to start, this test fails automatically.
    }
}
