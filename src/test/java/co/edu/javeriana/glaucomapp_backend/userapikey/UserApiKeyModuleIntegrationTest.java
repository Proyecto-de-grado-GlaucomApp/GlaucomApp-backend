package co.edu.javeriana.glaucomapp_backend.userapikey;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class UserApiKeyModuleIntegrationTest {
    /**
     * Verifies that the API Key module is correctly configured and initialized.
     */
    @Test
    void verifyModule() {

    }
}
