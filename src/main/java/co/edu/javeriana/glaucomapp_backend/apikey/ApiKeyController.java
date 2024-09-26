package co.edu.javeriana.glaucomapp_backend.apikey;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing API key operations such as generating and invalidating API keys.
 * <p>
 * This class provides endpoints for clients to generate new API keys and to invalidate existing keys.
 * </p>
 */
@RestController
@RequestMapping("/api-key")
public class ApiKeyController {

    private final ApiKeyServiceImpl apiKeyService;

    /**
     * Constructs an instance of {@link ApiKeyController}.
     *
     * @param apiKeyService the service to manage API keys
     */
    public ApiKeyController(ApiKeyServiceImpl apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Endpoint to generate a new API key.
     *
     * @param email the email associated with the new API key
     * @param entityName the name of the entity associated with the new API key
     * @return a {@link ResponseEntity} containing the created {@link ApiKeyEntity} with the new API key
     */
    @PostMapping("/generate-key")
    public ResponseEntity<ApiKeyEntity> generateApiKey(@RequestParam String email, 
                                                        @RequestParam String entityName) {
        ApiKeyEntity apiKeyEntity = apiKeyService.generateApiKey(email, entityName);
        return ResponseEntity.ok(apiKeyEntity);
    }

    /**
     * Endpoint to invalidate an existing API key.
     *
     * @param apiKey the API key to invalidate
     * @return a {@link ResponseEntity} with a confirmation message
     */
    @PostMapping("/invalidate-key")
    public ResponseEntity<String> invalidateApiKey(@RequestParam String apiKey) {
        apiKeyService.invalidateApiKey(apiKey);
        return ResponseEntity.ok("API Key invalidated successfully");
    }
}
