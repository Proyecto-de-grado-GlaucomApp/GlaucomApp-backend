package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyDTO;
import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyExternalService;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyApprovedException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyNotFoundException;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

/**
 * Service implementation for managing API keys, including generating new keys,
 * invalidating existing ones, and validating key status.
 * <p>
 * This class provides concrete implementations of the methods defined in 
 * the {@link ApiKeyExternalService} interface, utilizing the {@link ApiKeyManager}
 * for key generation and {@link ApiKeyRepository} for data persistence.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyExternalService, ApiKeyInternalService {

    private final ApiKeyManager apiKeyManager;
    
     private final ApiKeyRepository apiKeyRepository;

    /**
     * Generates a new API key for a user if they do not already have a pending or active key.
     * If the user has an existing API key with a status of PENDING or ACTIVE, throws 
     * {@link ApiKeyAlreadyExistsException}.
     *
     * @param userId the ID of the user requesting the API key
     * @return the generated {@link ApiKey} entity
     * @throws ApiKeyAlreadyExistsException if the user already has an active or pending key
     */
    @Override
    public ApiKey generateApiKeyByUser(Long userId) {
        // If the user has a key that is pending or active, throw an exception
        if (apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)).isPresent()) {
            throw new ApiKeyAlreadyExistsException("User already has an active or pending API key");
        }
        
        // Generate a new API key
        String apiKey = apiKeyManager.generateApiKey();
        ApiKey apiKeyEntity = new ApiKey();
        apiKeyEntity.setApiKey(apiKey);
        apiKeyEntity.setStatus(ApiKeyStatus.PENDING);
        apiKeyEntity.setUserApiId(userId);

        // Save the generated API key to the database
        try {
            return apiKeyRepository.save(apiKeyEntity);
        } catch (Exception e) {
            throw new RuntimeException("Error saving API key");
        }
    }

    /**
     * Finds whether an API key exists in the repository.
     *
     * @param apiKey the API key to search for
     * @return {@code true} if the API key exists; {@code false} otherwise
     */
    @Override
    public boolean findByApiKey(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey).isPresent();
    }

    /**
     * Validates if the provided API key is valid (i.e., exists and has an ACTIVE status).
     *
     * @param apiKey the API key to validate
     * @return {@code true} if the API key is valid and active; {@code false} otherwise
     */
    @Override
    public boolean isApiKeyValid(String apiKey) {
        Optional<ApiKey> apiKeyEntity = apiKeyRepository.findByApiKey(apiKey);
        return apiKeyEntity.isPresent() && apiKeyEntity.get().getStatus() == ApiKeyStatus.ACTIVE;
    }

    /**
     * Retrieves the API key information associated with a specific user.
     *
     * @param userId the ID of the user whose API key is to be retrieved
     * @return an {@link ApiKeyDTO} containing the API key and its status
     * @throws ApiKeyNotFoundException if no API key is found for the user
     */
    @Override
    public ApiKeyDTO getApiKeyByUser(Long userId) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE));
        Optional<ApiKey> apiKeyInactive = apiKeyRepository.findByUserApiIdAndStatus(userId, ApiKeyStatus.INACTIVE);

        if (apiKeyInactive.isPresent() && apiKey.isPresent()) {
            return new ApiKeyDTO(apiKey.get().getApiKey(), apiKey.get().getStatus().toString());
        } else if (apiKeyInactive.isPresent() && !apiKey.isPresent()) {
            return new ApiKeyDTO(apiKeyInactive.get().getApiKey(), apiKeyInactive.get().getStatus().toString());
        }
        if (apiKey.isEmpty()) {
            throw new ApiKeyNotFoundException("API key not found");
        }
        return new ApiKeyDTO(apiKey.get().getApiKey(), apiKey.get().getStatus().toString());
    }

    /**
     * Deletes the pending API key associated with a specific user.
     *
     * @param userId the ID of the user whose pending API key is to be deleted
     * @throws ApiKeyNotFoundException if no pending API key is found for the user
     */
    @Override
    public void deleteApiKeyByUser(Long userId) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByUserApiIdAndStatus(userId, ApiKeyStatus.PENDING);
        if (apiKey.isEmpty()) {
            apiKey = apiKeyRepository.findByUserApiIdAndStatus(userId, ApiKeyStatus.ACTIVE);
            if (apiKey.isEmpty()) {

            throw new ApiKeyNotFoundException("API key not found");
            }
        }
        try {
            apiKeyRepository.deleteById(apiKey.get().getId());
        } catch (Exception e) {
            throw new ApiKeyNotFoundException("API key not found for user");
        }
    }

    /**
     * Lists all approved (active) API keys.
     *
     * @return a list of {@link ApiKey} entities with ACTIVE status
     */
    @Override
    @RolesAllowed("ADMIN")
    public List<ApiKey> listApprovedApiKeys() {
        return apiKeyRepository.findByStatus(ApiKeyStatus.ACTIVE);
    }

    /**
     * Lists all pending API keys that have not been approved yet.
     *
     * @return a list of {@link ApiKey} entities with PENDING status
     */
    @Override
    @RolesAllowed("ADMIN")
    public List<ApiKey> listPendingApiKeys() {
        return apiKeyRepository.findByStatus(ApiKeyStatus.PENDING);
    }

    /**
     * Approves an API key by changing its status to ACTIVE.
     *
     * @param apiKeyId the ID of the API key to be approved
     * @return the approved {@link ApiKey} entity
     * @throws ApiKeyNotFoundException if the API key is not found
     * @throws ApiKeyAlreadyApprovedException if the API key is already active
     */
    @Override
    @RolesAllowed("ADMIN")
    public ApiKey approveApiKey(Long apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId).orElseThrow(() -> new ApiKeyNotFoundException("API Key not found"));
        if (apiKey.getStatus().equals(ApiKeyStatus.ACTIVE)) {
            throw new ApiKeyAlreadyApprovedException("API Key al;ready approved");
        }
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        return apiKeyRepository.save(apiKey);
    }

    @Override
    public ApiKey denyApiKey(Long apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId).orElseThrow(() -> new ApiKeyNotFoundException("API Key not found"));
        apiKey.setStatus(ApiKeyStatus.INACTIVE);

        Long userId = apiKey.getUserApiId();
        Optional<ApiKey> apiKeyInactive = apiKeyRepository.findByUserApiIdAndStatus(userId, ApiKeyStatus.INACTIVE);
        if (apiKeyInactive.isPresent()) {
            apiKeyRepository.delete(apiKeyInactive.get());
        }

        return apiKeyRepository.save(apiKey);
    }
}
