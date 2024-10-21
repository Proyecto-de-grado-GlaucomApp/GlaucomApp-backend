package co.edu.javeriana.glaucomapp_backend;

import static org.mockito.Mockito.mockStatic;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * Integration tests for the Glaucomapp Backend application.
 * <p>
 * This class is responsible for verifying the application context and ensuring that all
 * application modules are loaded correctly. It serves as a basic test to check that the
 * application can start without any issues.
 * </p>
 */
@ApplicationModuleTest
class GlaucomappBackendApplicationTests {

    /**
     * Tests that the application context loads successfully.
     * <p>
     * This test verifies that all modules within the GlaucomappBackend application are
     * configured correctly and can be instantiated without errors. It utilizes the
     * Spring Modulith framework to ensure that the application structure adheres to
     * the defined module boundaries.
     * </p>
     */
    @Test
    void contextLoads() {
        ApplicationModules.of(GlaucomappBackendApplication.class).verify();
    }


    	@Test
	void main() {
		try (var mockedSpringApplication = mockStatic(SpringApplication.class)) {
			GlaucomappBackendApplication.main(new String[]{});
            mockedSpringApplication.verify(() -> SpringApplication.run(GlaucomappBackendApplication.class, new String[]{}));
		}
	}

}
