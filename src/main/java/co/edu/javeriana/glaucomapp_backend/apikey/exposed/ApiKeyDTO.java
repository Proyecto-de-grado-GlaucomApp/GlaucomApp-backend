package co.edu.javeriana.glaucomapp_backend.apikey.exposed;

/**
 * Data Transfer Object (DTO) representing an API key and its associated metadata.
 * <p>
 * This record encapsulates the details of an API key, including its value,
 * the associated email, the entity name, and the active status of the API key.
 * It is used for transferring API key information between layers of the application.
 * </p>
 *
 * @param apiKey the unique API key string
 * @param email the email associated with the API key
 * @param entityName the name of the entity linked to the API key
 * @param isActive the active status of the API key
 */
public record ApiKeyDTO(
    String apiKey, 
    String email, 
    String entityName, 
    boolean isActive
) {}
