package co.edu.javeriana.glaucomapp_backend.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import co.edu.javeriana.glaucomapp_backend.apikey.exposed.ApiKeyExternalService;
import co.edu.javeriana.glaucomapp_backend.security.apikey.ClientAuthenticationHelper;

class ClientAuthenticationHelperTest {

    @Mock
    private ApiKeyExternalService apiKeyService;

    @InjectMocks
    private ClientAuthenticationHelper clientAuthenticationHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateApiKey_ValidApiKey() {
        String validApiKey = "valid-api-key";

        // Simula el comportamiento de apiKeyService
        when(apiKeyService.isApiKeyValid(validApiKey)).thenReturn(true);

        // Verifica que el método retorna true para una clave válida
        assertTrue(clientAuthenticationHelper.validateApiKey(validApiKey));
    }

    @Test
    void testValidateApiKey_InvalidApiKey() {
        String invalidApiKey = "invalid-api-key";

        // Simula el comportamiento de apiKeyService
        when(apiKeyService.isApiKeyValid(invalidApiKey)).thenReturn(false);

        // Verifica que el método retorna false para una clave inválida
        assertFalse(clientAuthenticationHelper.validateApiKey(invalidApiKey));
    }
}
