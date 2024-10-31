package co.edu.javeriana.glaucomapp_backend.apikey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ApiKey {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiKeyStatus status = ApiKeyStatus.PENDING;  // Using the enum for API key status with default value

    private Long userApiId;
}
