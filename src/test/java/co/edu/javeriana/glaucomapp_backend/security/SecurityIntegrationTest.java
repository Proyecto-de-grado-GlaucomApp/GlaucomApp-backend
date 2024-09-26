package co.edu.javeriana.glaucomapp_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * Integration test for the security module.
 * <p>
 * This class is responsible for testing the integration of the security
 * features within the application module. It utilizes Spring's Modulith
 * testing framework to bootstrap the application context and verify
 * module functionality.
 * </p>
 */
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
public class SecurityIntegrationTest {

    /**
     * Verifies that the security module is correctly configured and initialized.
     */
    @Test
    void verifyModule() {

    }
}
