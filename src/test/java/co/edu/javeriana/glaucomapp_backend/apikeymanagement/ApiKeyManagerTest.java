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
    void testGenerateApiKey_NotNull() {
        String apiKey = apiKeyManager.generateApiKey();
        assertNotNull(apiKey, "Generated API key should not be null");
    }

    @Test
    void testGenerateApiKey_Length() {
        String apiKey = apiKeyManager.generateApiKey();
        int expectedLength = 43; // Base64 encoding of 32 bytes results in 43 characters without padding
        assertEquals(expectedLength, apiKey.length(), "Generated API key should have the correct length");
    }

    @Test
    void testGenerateApiKey_Unique() {
        String apiKey1 = apiKeyManager.generateApiKey();
        String apiKey2 = apiKeyManager.generateApiKey();
        assertNotEquals(apiKey1, apiKey2, "Generated API keys should be unique");
    }
}