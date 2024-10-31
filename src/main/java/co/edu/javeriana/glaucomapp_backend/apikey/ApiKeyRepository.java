package co.edu.javeriana.glaucomapp_backend.apikey;

import java.util.Optional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing and managing {@link ApiKey} entities in the database.
 * <p>
 * This interface extends {@link JpaRepository} to provide basic CRUD operations as well as
 * additional custom query methods for API key management.
 * </p>
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Retrieves an {@link ApiKey} by its API key.
     *
     * @param apiKey the API key to search for
     * @return an {@link Optional} containing the matching {@link ApiKey}, or an empty {@link Optional} if not found
     */
    Optional<ApiKey> findByApiKey(String apiKey);


    /**
     * Finds an ApiKeyEntity by the given email.
     *
     * @param email the email to search for
     * @return an Optional containing the found ApiKeyEntity, or an empty Optional if no entity is found
     */
    //Optional<ApiKey> findByEmail(String email);


    List<ApiKey> findByStatus(ApiKeyStatus status);


    Optional<ApiKey> findByUserApiIdAndStatusIn(Long userId, List<ApiKeyStatus> status);

    //Buscar por userId y Status
    Optional<ApiKey> findByUserApiIdAndStatus(Long userId, ApiKeyStatus status);
    //Eliminar por id de apikey


    Optional<ApiKey> findByUserApiId(Long userId);
}
