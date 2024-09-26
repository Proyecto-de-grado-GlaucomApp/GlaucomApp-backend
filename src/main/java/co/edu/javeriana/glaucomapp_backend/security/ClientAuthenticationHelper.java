package co.edu.javeriana.glaucomapp_backend.security;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.apikey.exposed.ApiKeyService;
import lombok.RequiredArgsConstructor;

/**
 * Service for handling client authentication based on API keys.
 * <p>
 * This service provides functionality to validate API keys against the repository
 * and determines whether a client is authenticated based on the provided API key.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ClientAuthenticationHelper {

    private final ApiKeyService apiKeyService;

    /**
     * Validates the provided API key by checking its existence in the repository.
     *
     * @param apiKey the API key to be validated
     * @return true if the API key exists and is active, false otherwise
     */
    public boolean validateApiKey(String apiKey) {
        // Check if the API key exists in the repository
        boolean isValid = apiKeyService.findByApiKey(apiKey);
        return isValid;
    }
}
