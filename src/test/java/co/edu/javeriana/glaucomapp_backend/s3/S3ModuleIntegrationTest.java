package co.edu.javeriana.glaucomapp_backend.s3;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class S3ModuleIntegrationTest {
    @Test
    void verifyModule() {

    }
}
