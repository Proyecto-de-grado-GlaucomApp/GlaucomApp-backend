package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyDTO;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyNotFoundException;

// This class contains tests for the ApiKeyServiceImpl class, which manages API key generation and validation.
public class ApiKeyServiceImplTest {

    // Mock objects for dependencies
    @Mock
    private ApiKeyManager apiKeyManager; // Mocked dependency to manage API key generation

    @Mock
    private ApiKeyRepository apiKeyRepository; // Mocked dependency to interact with the API key data repository

    @InjectMocks
    private ApiKeyServiceImpl apiKeyService; // Service under test, with mocked dependencies injected

    // Set up the test environment before each test
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes the mock objects
    }

    // Test generating a new API key when no active or pending key exists for a user
    @Test
    public void testGenerateApiKeyWhenNoExistingKey() {
        // Arrange
        Long userId = 1L; // Sample user ID for testing
        when(apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        when(apiKeyManager.generateApiKey()).thenReturn("new-api-key");

        ApiKey expectedApiKey = new ApiKey();
        expectedApiKey.setApiKey("new-api-key");
        expectedApiKey.setStatus(ApiKeyStatus.PENDING);
        expectedApiKey.setUserApiId(userId);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(expectedApiKey);

        // Act
        ApiKey result = apiKeyService.generateApiKeyByUser(userId); // Call the service method to generate an API key

        // Assert
        assertNotNull(result); // Check if the result is not null
        assertEquals("new-api-key", result.getApiKey()); // Check the generated API key value
        assertEquals(ApiKeyStatus.PENDING, result.getStatus()); // Check the API key status
        assertEquals(userId, result.getUserApiId()); // Check the associated user ID
    }

    // Test attempting to generate an API key for a user who already has an active or pending key
    @Test
    public void testGenerateApiKeyWhenExistingKeyThrowsException() {
        // Arrange
        Long userId = 1L; // Sample user ID for testing
        when(apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)))
                .thenReturn(Optional.of(new ApiKey()));

        // Act & Assert
        ApiKeyAlreadyExistsException exception = assertThrows(ApiKeyAlreadyExistsException.class, () -> {
            apiKeyService.generateApiKeyByUser(userId); // Call the service method
        });
        assertNotNull(exception); // Check if the exception is thrown
    }

    // Test saving a newly generated API key to the repository successfully
    @Test
    public void testSaveNewlyGeneratedApiKey() {
        // Arrange
        Long userId = 1L; // Sample user ID for testing
        when(apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        when(apiKeyManager.generateApiKey()).thenReturn("new-api-key");

        ApiKey expectedApiKey = new ApiKey();
        expectedApiKey.setApiKey("new-api-key");
        expectedApiKey.setStatus(ApiKeyStatus.PENDING);
        expectedApiKey.setUserApiId(userId);
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(expectedApiKey);

        // Act
        ApiKey result = apiKeyService.generateApiKeyByUser(userId); // Call the service method to generate an API key

        // Assert
        assertNotNull(result); // Check if the result is not null
        assertEquals("new-api-key", result.getApiKey()); // Check the generated API key value
        assertEquals(ApiKeyStatus.PENDING, result.getStatus()); // Check the API key status
        assertEquals(userId, result.getUserApiId()); // Check the associated user ID
    }

    // Test retrieving an API key by user ID when it exists
    @Test
    public void testRetrieveApiKeyWhenExists() {
        // Arrange
        Long userId = 1L; // Sample user ID for testing
        ApiKey apiKey = new ApiKey();
        apiKey.setApiKey("test-api-key"); // Set the test API key value
        apiKey.setStatus(ApiKeyStatus.ACTIVE); // Set the status to ACTIVE
        apiKey.setUserApiId(userId); // Associate the API key with the user ID
        when(apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)))
                .thenReturn(Optional.of(apiKey));

        // Act
        ApiKeyDTO result = apiKeyService.getApiKeyByUser(userId); // Call the service method to retrieve the API key

        // Assert
        assertNotNull(result); // Check if the result is not null
        assertEquals("test-api-key", result.apiKey()); // Check the API key value
        assertEquals(ApiKeyStatus.ACTIVE.toString(), result.status()); // Check the API key status
    }

    // Test validating an API key as active and existing
    @Test
    public void testValidateExistingActiveApiKey() {
        // Arrange
        String apiKey = "existing-api-key"; // Sample existing API key for testing
        ApiKey existingApiKey = new ApiKey();
        existingApiKey.setApiKey(apiKey); // Set the existing API key value
        existingApiKey.setStatus(ApiKeyStatus.ACTIVE); // Set the status to ACTIVE
        when(apiKeyRepository.findByApiKey(apiKey)).thenReturn(Optional.of(existingApiKey));

        // Act
        boolean result = apiKeyService.isApiKeyValid(apiKey); // Call the service method to validate the API key

        // Assert
        assertTrue(result); // Check if the validation result is true
    }

    // Test approving a pending API key and changing its status to active
    @Test
    public void testApprovePendingApiKeySuccess() {
        // Arrange
        Long apiKeyId = 1L; // Sample API key ID for testing
        ApiKey apiKey = new ApiKey();
        apiKey.setId(apiKeyId); // Set the API key ID
        apiKey.setStatus(ApiKeyStatus.PENDING); // Set the status to PENDING
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(Optional.of(apiKey));
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        // Act
        ApiKey result = apiKeyService.approveApiKey(apiKeyId); // Call the service method to approve the API key

        // Assert
        assertNotNull(result); // Check if the result is not null
        assertEquals(ApiKeyStatus.ACTIVE, result.getStatus()); // Check if the status changed to ACTIVE
    }

    // Test validating an API key that does not exist
    @Test
    public void testValidateNonExistingApiKey() {
        // Arrange
        String nonExistingApiKey = "non-existing-key"; // Sample non-existing API key for testing
        when(apiKeyRepository.findByApiKey(nonExistingApiKey)).thenReturn(Optional.empty());

        // Act
        boolean result = apiKeyService.isApiKeyValid(nonExistingApiKey); // Call the service method to validate the API key

        // Assert
        assertFalse(result); // Check if the validation result is false
    }

    // Test deleting a pending API key for a user who has no pending key
    @Test
    public void testDeletePendingApiKeyWhenNoPendingKey() {
        // Arrange
        Long userId = 1L; // Sample user ID for testing
        when(apiKeyRepository.findByUserApiIdAndStatus(userId, ApiKeyStatus.PENDING)).thenReturn(Optional.empty());

        // Act & Assert
        ApiKeyNotFoundException exception = assertThrows(ApiKeyNotFoundException.class,
                () -> apiKeyService.deleteApiKeyByUser(userId)); // Call the service method to delete the API key
        assertNotNull(exception); // Check if the exception is thrown
    }

    // Test deleting an API key by user throws exception on delete failure
@Test
public void testDeleteApiKeyByUserThrowsExceptionOnDeleteFailure() {
    // Arrange: Configurar el entorno y las expectativas
    Long userId = 1L; // ID del usuario para el cual se eliminará la API key
    ApiKey pendingApiKey = new ApiKey();
    pendingApiKey.setId(1L); // ID de la API key pendiente
    pendingApiKey.setUserApiId(userId);
    pendingApiKey.setStatus(ApiKeyStatus.PENDING);

    // Simular el comportamiento del repositorio
    when(apiKeyRepository.findByUserApiIdAndStatus(userId, ApiKeyStatus.PENDING)).thenReturn(Optional.of(pendingApiKey));
    doThrow(new RuntimeException("Deletion failed")).when(apiKeyRepository).deleteById(pendingApiKey.getId());

    // Act: Llamar al método que se está probando
    // Assert: Verificar que se lanza la excepción esperada
    assertThrows(RuntimeException.class,
            () -> apiKeyService.deleteApiKeyByUser(userId)); // Llamada al método para eliminar la API key
}

}
