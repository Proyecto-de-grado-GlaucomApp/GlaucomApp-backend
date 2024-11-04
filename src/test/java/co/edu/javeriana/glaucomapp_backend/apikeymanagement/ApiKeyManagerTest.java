package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApiKeyManagerTest {

    private ApiKeyManager apiKeyManager;

    @BeforeEach
    void setUp() {
        apiKeyManager = new ApiKeyManager();
    }

    @Test
    void testGenerateApiKey() {
        // Generar la clave API
        String apiKey = apiKeyManager.generateApiKey();

        // Verificaciones
        assertNotNull(apiKey, "Generated API key should not be null");
        assertEquals(43, apiKey.length(), "Generated API key should have the correct length");
    }

    @Test
    void testGenerateApiKey_Unique() {
        // Generar dos claves API y verificar que son únicas
        String apiKey1 = apiKeyManager.generateApiKey();
        String apiKey2 = apiKeyManager.generateApiKey();
        assertNotEquals(apiKey1, apiKey2, "Generated API keys should be unique");
    }
}
