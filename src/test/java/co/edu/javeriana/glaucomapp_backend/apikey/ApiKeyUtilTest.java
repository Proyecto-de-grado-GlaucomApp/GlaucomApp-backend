package co.edu.javeriana.glaucomapp_backend.apikey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import co.edu.javeriana.glaucomapp_backend.apikey.exposed.ApiKeyDTO;
import org.junit.jupiter.api.Test;

public class ApiKeyUtilTest {

    @Test
    void testMapApiKeyDTO_ValidApiKey() {
        // Arrange
        ApiKey apiKey = new ApiKey();
        apiKey.setApiKey("testApiKey");
        apiKey.setStatus(ApiKeyStatus.ACTIVE);

        // Act
        ApiKeyDTO apiKeyDTO = ApiKeyUtil.mapApiKeyDTO(apiKey);

        // Assert
        assertEquals("testApiKey", apiKeyDTO.apiKey());
        assertEquals("ACTIVE", apiKeyDTO.status());
    }

    @Test
    void testMapApiKeyDTO_NullApiKey() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            ApiKeyUtil.mapApiKeyDTO(null);
        });
    }
}