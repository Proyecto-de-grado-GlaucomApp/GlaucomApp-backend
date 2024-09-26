package co.edu.javeriana.glaucomapp_backend.apikey;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing and managing {@link ApiKeyEntity} entities in the database.
 * <p>
 * This interface extends {@link JpaRepository} to provide basic CRUD operations as well as
 * additional custom query methods for API key management.
 * </p>
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    /**
     * Retrieves an {@link ApiKeyEntity} by its API key.
     *
     * @param apiKey the API key to search for
     * @return an {@link Optional} containing the matching {@link ApiKeyEntity}, or an empty {@link Optional} if not found
     */
    Optional<ApiKeyEntity> findByApiKey(String apiKey);

    /**
     * Retrieves an {@link ApiKeyEntity} by its API key and active status.
     *
     * @param apiKey the API key to search for
     * @param b the active status to filter by
     * @return an {@link Optional} containing the matching {@link ApiKeyEntity}, or an empty {@link Optional} if not found
     */
    Optional<ApiKeyEntity> findByApiKeyAndActive(String apiKey, boolean b);
}
