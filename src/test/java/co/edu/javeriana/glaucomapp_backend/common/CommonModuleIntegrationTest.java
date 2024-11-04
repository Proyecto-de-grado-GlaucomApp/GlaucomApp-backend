package co.edu.javeriana.glaucomapp_backend.common;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class CommonModuleIntegrationTest {
    @Test
    void verifyModule() {

    }
}
