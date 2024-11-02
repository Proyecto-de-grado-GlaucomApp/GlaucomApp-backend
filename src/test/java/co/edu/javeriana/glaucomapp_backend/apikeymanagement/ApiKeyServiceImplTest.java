package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyDTO;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyApprovedException;
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
        // Mock repository to return empty Optional when checking for existing keys
        when(apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        // Mock API key generation to return a new API key
        when(apiKeyManager.generateApiKey()).thenReturn("new-api-key");

        // Create expected API key object with predefined values
        ApiKey expectedApiKey = new ApiKey();
        expectedApiKey.setApiKey("new-api-key");
        expectedApiKey.setStatus(ApiKeyStatus.PENDING);
        expectedApiKey.setUserApiId(userId);
        // Mock repository save method to return the expected API key
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(expectedApiKey);

        // Act
        ApiKey result = apiKeyService.generateApiKeyByUser(userId); // Call the service method to generate an API key

        // Assert
        assertNotNull(result); // Check if the result is not null
        assertEquals("new-api-key", result.getApiKey()); // Check the generated API key value
        assertEquals(ApiKeyStatus.PENDING, result.getStatus()); // Check the API key status
        assertEquals(userId, result.getUserApiId()); // Check the associated user ID
    }

    // Test attempting to generate an API key for a user who already has an active
    // or pending key
    @Test
    public void testGenerateApiKeyWhenExistingKeyThrowsException() {
        // Arrange
        Long userId = 1L; // Sample user ID for testing
        // Mock repository to return an existing API key
        when(apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)))
                .thenReturn(Optional.of(new ApiKey()));

        // Act & Assert
        // Expect ApiKeyAlreadyExistsException when trying to generate a new API key
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
        // Mock repository to return empty Optional when checking for existing keys
        when(apiKeyRepository.findByUserApiIdAndStatusIn(userId, List.of(ApiKeyStatus.PENDING, ApiKeyStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        // Mock API key generation to return a new API key
        when(apiKeyManager.generateApiKey()).thenReturn("new-api-key");

        // Create expected API key object
        ApiKey expectedApiKey = new ApiKey();
        expectedApiKey.setApiKey("new-api-key");
        expectedApiKey.setStatus(ApiKeyStatus.PENDING);
        expectedApiKey.setUserApiId(userId);
        // Mock repository save method to return the expected API key
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
        // Mock repository to return the existing API key
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
        // Mock repository to return the existing API key
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
        // Mock repository to return the existing API key
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(Optional.of(apiKey));
        // Mock repository save method to return the same API key
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
        // Mock repository to return empty Optional
        when(apiKeyRepository.findByApiKey(nonExistingApiKey)).thenReturn(Optional.empty());

        // Act
        boolean result = apiKeyService.isApiKeyValid(nonExistingApiKey); // Call the service method to validate the API
                                                                         // key

        // Assert
        assertFalse(result); // Check if the validation result is false
    }

    // Test deleting a pending API key for a user who has no pending key
    @Test
    public void testDeletePendingApiKeyWhenNoPendingKey() {
        // Arrange
        Long userId = 1L; // Sample user ID for testing
        // Mock repository to return empty Optional when checking for pending keys
        when(apiKeyRepository.findByUserApiIdAndStatus(userId, ApiKeyStatus.PENDING)).thenReturn(Optional.empty());

        // Act & Assert
        // Expect ApiKeyNotFoundException when trying to delete a non-existing pending
        // API key
        ApiKeyNotFoundException exception = assertThrows(ApiKeyNotFoundException.class,
                () -> apiKeyService.deleteApiKeyByUser(userId));
        assertNotNull(exception); // Check if the exception is thrown
    }

    // Test approving an API key that is already active
    @Test
    public void testApproveAlreadyActiveApiKey() {
        // Arrange
        Long apiKeyId = 1L; // Sample API key ID for testing
        ApiKey activeApiKey = new ApiKey();
        activeApiKey.setStatus(ApiKeyStatus.ACTIVE); // Set the status to ACTIVE
        // Mock repository to return the active API key
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(Optional.of(activeApiKey));

        // Act & Assert
        // Expect ApiKeyAlreadyApprovedException when trying to approve an already
        // active API key
        ApiKeyAlreadyApprovedException exception = assertThrows(ApiKeyAlreadyApprovedException.class,
                () -> apiKeyService.approveApiKey(apiKeyId));
        assertNotNull(exception); // Check if the exception is thrown
    }

    // Test the validation of a non-existing API key
    @Test
    public void testFindByApiKeyNotFound() {
        // Arrange
        String nonExistingApiKey = "non-existing-key"; // API key that does not exist
        // Simulate that the repository does not find the API key
        when(apiKeyRepository.findByApiKey(nonExistingApiKey)).thenReturn(Optional.empty());

        // Act
        boolean result = apiKeyService.findByApiKey(nonExistingApiKey); // Call the method to validate the API key

        // Assert
        assertFalse(result); // Check that the validation result is false
    }

    // Test the validation of an existing API key
    @Test
    public void testFindByApiKeyFound() {
        // Arrange
        String existingApiKey = "existing-key"; // API key that exists
        ApiKey apiKey = new ApiKey();
        apiKey.setApiKey(existingApiKey); // Set the value of the API key
        // Simulate that the repository finds the API key
        when(apiKeyRepository.findByApiKey(existingApiKey)).thenReturn(Optional.of(apiKey));

        // Act
        boolean result = apiKeyService.findByApiKey(existingApiKey); // Call the method to validate the API key

        // Assert
        assertTrue(result); // Check that the validation result is true
    }

    // Test the list of approved API keys
    @Test
    public void testListApprovedApiKeys() {
        // Arrange
        List<ApiKey> approvedKeys = List.of(new ApiKey(), new ApiKey()); // Simulated list of approved API keys
        // Simulate that the repository returns the list of approved API keys
        when(apiKeyRepository.findByStatus(ApiKeyStatus.ACTIVE)).thenReturn(approvedKeys);

        // Act
        List<ApiKey> result = apiKeyService.listApprovedApiKeys(); // Call the method to list the approved API keys

        // Assert
        assertNotNull(result); // Check that the result is not null
        assertEquals(approvedKeys.size(), result.size()); // Check that the list size matches
    }

    // Test the list of pending API keys
    @Test
    public void testListPendingApiKeys() {
        // Arrange
        List<ApiKey> pendingKeys = List.of(new ApiKey(), new ApiKey()); // Simulated list of pending API keys
        // Simulate that the repository returns the list of pending API keys
        when(apiKeyRepository.findByStatus(ApiKeyStatus.PENDING)).thenReturn(pendingKeys);

        // Act
        List<ApiKey> result = apiKeyService.listPendingApiKeys(); // Call the method to list the pending API keys

        // Assert
        assertNotNull(result); // Check that the result is not null
        assertEquals(pendingKeys.size(), result.size()); // Check that the list size matches
    }

    // Test the approval of an API key that exists and is pending
    @Test
    public void testApproveApiKeySuccess() {
        // Arrange
        Long apiKeyId = 1L; // Example API key ID
        ApiKey apiKey = new ApiKey();
        apiKey.setId(apiKeyId); // Set the ID of the API key
        apiKey.setStatus(ApiKeyStatus.PENDING); // Set the initial status to PENDING
        // Simulate that the repository finds the API key
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(Optional.of(apiKey));
        // Simulate that the repository saves the updated API key
        when(apiKeyRepository.save(apiKey)).thenReturn(apiKey);

        // Act
        ApiKey result = apiKeyService.approveApiKey(apiKeyId); // Call the method to approve the API key

        // Assert
        assertNotNull(result); // Check that the result is not null
        assertEquals(ApiKeyStatus.ACTIVE, result.getStatus()); // Check that the status is now ACTIVE
    }

    // Test the approval of an API key that does not exist
    @Test
    public void testApproveApiKeyNotFound() {
        // Arrange
        Long nonExistingApiKeyId = 999L; // Example non-existing API key ID
        // Simulate that the repository does not find the API key
        when(apiKeyRepository.findById(nonExistingApiKeyId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiKeyNotFoundException exception = assertThrows(ApiKeyNotFoundException.class, () -> {
            apiKeyService.approveApiKey(nonExistingApiKeyId); // Call the method to approve the API key
        });
        assertNotNull(exception); // Check if the exception is thrown
    }

    // Test the approval of an API key that is already approved
    @Test
    public void testApproveApiKeyAlreadyApproved() {
        // Arrange
        Long apiKeyId = 2L; // Example API key ID
        ApiKey apiKey = new ApiKey();
        apiKey.setId(apiKeyId); // Set the ID of the API key
        apiKey.setStatus(ApiKeyStatus.ACTIVE); // Set the initial status to ACTIVE
        // Simulate that the repository finds the API key
        when(apiKeyRepository.findById(apiKeyId)).thenReturn(Optional.of(apiKey));

        // Act & Assert
        ApiKeyAlreadyApprovedException exception = assertThrows(ApiKeyAlreadyApprovedException.class, () -> {
            apiKeyService.approveApiKey(apiKeyId); // Call the method to approve the API key
        });
        assertNotNull(exception); // Check if the exception is thrown
    }

}
