package co.edu.javeriana.glaucomapp_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for the security module.
 * <p>
 * This class is responsible for testing the integration of the Security module.
 * It uses Spring Modulith's testing framework to verify that the module functions correctly
 * within the application context.
 * </p>
 */
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class SecurityModuleIntegrationTest {

    /**
     * Verifies that the security module is correctly configured and initialized.
     */
    @Test
    void verifyModule() {

    }
}
