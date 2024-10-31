package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for the Glaucoma Screening module.
 * <p>
 * This class is responsible for testing the integration of the Glaucoma Screening module.
 * It uses Spring Modulith's testing framework to verify that the module functions correctly
 * within the application context.
 * </p>
 */
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class GlaucomaScreeningModuleIntegrationTest {

    /**
     * Verifies the integrity of the Glaucoma Screening module.
     */
    @Test
    void verifyModule() {

    }
}
