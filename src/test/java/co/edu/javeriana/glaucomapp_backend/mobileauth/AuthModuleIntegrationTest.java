package co.edu.javeriana.glaucomapp_backend.mobileauth;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

import co.edu.javeriana.glaucomapp_backend.TestSecurityConfig;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)  // Añade esta línea
public class AuthModuleIntegrationTest {
    @Test
    void verifyModule() {
    }
}