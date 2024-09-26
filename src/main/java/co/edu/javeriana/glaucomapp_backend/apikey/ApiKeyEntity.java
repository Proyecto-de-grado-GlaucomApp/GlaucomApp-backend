package co.edu.javeriana.glaucomapp_backend.apikey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Entity representing an API key and its associated metadata.
 *
 * <p>This entity is responsible for storing API keys in the database,
 * along with their related attributes such as the email, associated entity name, and
 * activation status.</p>
 */
@Data
@Entity
public class ApiKeyEntity {

    /**
     * Unique identifier for the API key entity.
     * This ID is automatically generated and incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The string representing the API key.
     * This field must be unique and cannot be null.
     */
    @Column(nullable = false, unique = true)
    private String apiKey;

    /**
     * The email address associated with this API key.
     * This field must be unique and cannot be null.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The name of the entity that is linked to this API key.
     * This field cannot be null.
     */
    @Column(nullable = false)
    private String entityName;

    /**
     * Indicates whether the API key is currently active.
     * This field cannot be null and holds a boolean value.
     */
    @Column(nullable = false)
    private boolean active;
}
