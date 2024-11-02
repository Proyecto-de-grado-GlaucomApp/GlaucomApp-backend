package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import java.util.List;

import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyDTO;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyApprovedException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyNotFoundException;

public interface ApiKeyInternalService {

    /**
     * Generates a new API key for the specified user.
     *
     * @param userId the ID of the user for whom the API key is to be generated
     * @return the generated ApiKey
     * @throws ApiKeyAlreadyExistsException if the user already has an active or pending API key
     */
    ApiKey generateApiKeyByUser(Long userId);

    /**
     * Checks if the specified API key exists in the repository.
     *
     * @param apiKey the API key to search for
     * @return {@code true} if the API key exists; {@code false} otherwise
     */
    boolean findByApiKey(String apiKey);

    /**
     * Checks if the specified API key is valid (i.e., exists and is active).
     *
     * @param apiKey the API key to validate
     * @return {@code true} if the API key is valid; {@code false} otherwise
     */
    boolean isApiKeyValid(String apiKey);

    /**
     * Retrieves the API key and its status for the specified user.
     *
     * @param userId the ID of the user
     * @return the API key data transfer object
     * @throws ApiKeyNotFoundException if the API key is not found
     */
    ApiKeyDTO getApiKeyByUser(Long userId);

    /**
     * Deletes the API key for the specified user.
     *
     * @param userId the ID of the user whose API key is to be deleted
     * @throws ApiKeyNotFoundException if no API key is found for the user
     */
    void deleteApiKeyByUser(Long userId);

    /**
     * Lists all approved API keys.
     *
     * @return a list of active API keys
     */
    List<ApiKey> listApprovedApiKeys();

    /**
     * Lists all pending API keys.
     *
     * @return a list of pending API keys
     */
    List<ApiKey> listPendingApiKeys();

    /**
     * Approves the specified API key.
     *
     * @param apiKeyId the ID of the API key to approve
     * @return the approved ApiKey
     * @throws ApiKeyNotFoundException if the API key is not found
     * @throws ApiKeyAlreadyApprovedException if the API key is already approved
     */
    ApiKey approveApiKey(Long apiKeyId);
}
