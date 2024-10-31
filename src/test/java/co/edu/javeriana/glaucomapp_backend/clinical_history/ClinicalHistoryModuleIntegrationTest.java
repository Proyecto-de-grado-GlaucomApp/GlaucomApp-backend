package co.edu.javeriana.glaucomapp_backend.clinical_history;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class ClinicalHistoryModuleIntegrationTest {
    @Test
    void verifyModule() {

    }
}
