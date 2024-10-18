package co.edu.javeriana.glaucomapp_backend.apikey.exposed;

/**
 * Service interface for managing API key operations.
 * <p>
 * This interface defines the methods for generating, invalidating, and validating
 * API keys, as well as retrieving API key information. It serves as a contract for
 * any implementation of API key management functionality.
 * </p>
 */
public interface ApiKeyExternalService {
    
    boolean isApiKeyValid(String apiKey);
    


}
