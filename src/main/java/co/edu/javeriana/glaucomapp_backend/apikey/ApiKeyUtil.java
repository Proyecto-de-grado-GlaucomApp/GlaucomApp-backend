package co.edu.javeriana.glaucomapp_backend.apikey;

import co.edu.javeriana.glaucomapp_backend.apikey.exposed.ApiKeyDTO;
import lombok.experimental.UtilityClass;

/**
 * Utility class for API key-related operations.
 * <p>
 * This class provides static methods for handling API key entities and 
 * converting them to Data Transfer Objects (DTOs). It is designed to facilitate
 * the mapping of data between entity and DTO representations.
 * </p>
 */
@UtilityClass
public class ApiKeyUtil {

    /**
     * Maps an {@link ApiKeyEntity} to an {@link ApiKeyDTO}.
     *
     * @param apiKey the {@link ApiKeyEntity} to be mapped
     * @return an {@link ApiKeyDTO} containing the same data as the provided entity
     */
    public static ApiKeyDTO mapApiKeyDTO(ApiKeyEntity apiKey) {
        return new ApiKeyDTO(apiKey.getApiKey(), apiKey.getEmail(), apiKey.getEntityName(), apiKey.isActive());
    }
}
