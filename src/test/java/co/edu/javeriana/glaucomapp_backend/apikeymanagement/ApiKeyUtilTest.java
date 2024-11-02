package co.edu.javeriana.glaucomapp_backend.apikeymanagement;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyDTO;

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

}