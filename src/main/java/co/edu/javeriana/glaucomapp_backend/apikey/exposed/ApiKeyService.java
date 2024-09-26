package co.edu.javeriana.glaucomapp_backend.apikey.exposed;

import co.edu.javeriana.glaucomapp_backend.apikey.ApiKeyEntity;

/**
 * Service interface for managing API key operations.
 * <p>
 * This interface defines the methods for generating, invalidating, and validating
 * API keys, as well as retrieving API key information. It serves as a contract for
 * any implementation of API key management functionality.
 * </p>
 */
public interface ApiKeyService {
    
    /**
     * Generates a new API key for the specified email and entity name.
     *
     * @param email the email associated with the new API key
     * @param entityName the name of the entity linked to the new API key
     * @return the generated {@link ApiKeyEntity} containing the new API key and its metadata
     */
    ApiKeyEntity generateApiKey(String email, String entityName);

    /**
     * Invalidates the specified API key.
     *
     * @param apiKey the API key to be invalidated
     */
    void invalidateApiKey(String apiKey);

    /**
     * Checks if the specified API key is valid.
     *
     * @param apiKey the API key to be validated
     * @return {@code true} if the API key is valid; {@code false} otherwise
     */
    boolean isApiKeyValid(String apiKey);

    /**
     * Retrieves the API key status by its key.
     *
     * @param apiKey the API key to search for
     * @return {@code true} if the API key exists; {@code false} otherwise
     */
    boolean findByApiKey(String apiKey);
}
