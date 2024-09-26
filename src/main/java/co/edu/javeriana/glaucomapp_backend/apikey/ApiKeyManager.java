package co.edu.javeriana.glaucomapp_backend.apikey;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * Component for generating secure API keys.
 * <p>
 * This class provides functionality to generate a unique API key of a specified length,
 * ensuring the keys are securely randomized.
 * </p>
 */
@Component
public class ApiKeyManager {
    // Length of the generated API key in bytes
    private static final int API_KEY_LENGTH = 32;

    /**
     * Generates a new secure API key.
     *
     * @return a randomly generated API key encoded in a URL-safe Base64 format
     */
    public String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[API_KEY_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
