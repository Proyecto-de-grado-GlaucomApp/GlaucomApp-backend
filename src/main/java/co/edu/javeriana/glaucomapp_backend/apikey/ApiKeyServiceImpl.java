package co.edu.javeriana.glaucomapp_backend.apikey;

import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.apikey.exceptions.EmailAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.apikey.exposed.ApiKeyService;
import lombok.RequiredArgsConstructor;

/**
 * Service implementation for managing API keys, including generating new keys,
 * invalidating existing ones, and validating key status.
 * <p>
 * This class provides concrete implementations of the methods defined in 
 * the {@link ApiKeyService} interface, utilizing the {@link ApiKeyManager}
 * for key generation and {@link ApiKeyRepository} for data persistence.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyManager apiKeyManager;
    private final ApiKeyRepository apiKeyRepository;

    /**
     * Generates a new API key and saves it to the repository.
     * Ensures the generated API key is unique.
     *
     * @param email the email associated with the API key
     * @param entityName the name of the entity associated with the API key
     * @return the created {@link ApiKeyEntity} with the new API key
     */
    @Override
    public ApiKeyEntity generateApiKey(String email, String entityName) {
        // Check if the email is already associated with an existing API key
        if (apiKeyRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("The email address is already associated with an API key.");
        }

        String apiKey = apiKeyManager.generateApiKey();
        ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
        apiKeyEntity.setApiKey(apiKey);
        apiKeyEntity.setEmail(email);
        apiKeyEntity.setEntityName(entityName);
        apiKeyEntity.setActive(true);

        try {
            ApiKeyEntity savedEntity = apiKeyRepository.save(apiKeyEntity);
            return savedEntity;
        } catch (Exception e) {
            throw e; // Consider logging the exception as well
        }
    }

    /**
     * Invalidates an existing API key by setting its active status to false.
     *
     * @param apiKey the API key to invalidate
     */
    @Override
    public void invalidateApiKey(String apiKey) {
        Optional<ApiKeyEntity> apiKeyEntity = apiKeyRepository.findByApiKey(apiKey);
        apiKeyEntity.ifPresent(entity -> {
            entity.setActive(false);
            apiKeyRepository.save(entity);
        });
    }

    /**
     * Checks if the specified API key exists in the repository.
     *
     * @param apiKey the API key to search for
     * @return {@code true} if the API key exists; {@code false} otherwise
     */
    @Override
    public boolean findByApiKey(String apiKey) {
        return apiKeyRepository.findByApiKey(apiKey).isPresent();
    }

    /**
     * Checks if the specified API key is valid (i.e., exists and is active).
     *
     * @param apiKey the API key to validate
     * @return {@code true} if the API key is valid; {@code false} otherwise
     */
    @Override
    public boolean isApiKeyValid(String apiKey) {
        return apiKeyRepository.findByApiKeyAndActive(apiKey, true).isPresent();
    }
}
