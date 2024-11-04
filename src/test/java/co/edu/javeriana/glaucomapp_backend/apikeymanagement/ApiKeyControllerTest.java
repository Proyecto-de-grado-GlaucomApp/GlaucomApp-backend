package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyDTO;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyApprovedException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyNotFoundException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;

class ApiKeyControllerTest {

    @InjectMocks
    private ApiKeyController apiKeyController;

    @Mock
    private ApiKeyInternalService apiKeyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes the mock objects
    }

    // Unit Tests for generateApiKey
    @Test
    void generateApiKey_ShouldReturnCreatedResponse_WhenApiKeyIsGenerated() {
        // Arrange
        Long userId = 1L;
        ApiKey mockApiKey = new ApiKey(); // Mock or instantiate your ApiKey object
        when(apiKeyService.generateApiKeyByUser(userId)).thenReturn(mockApiKey);

        // Act
        ResponseEntity<ApiKey> response = apiKeyController.generateApiKey(userId);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode()); // Verify response status
        assertEquals(mockApiKey, response.getBody()); // Verify response body
    }

    // Unit Tests for generateApiKey with UnauthorizedException thrown by the service method
    @Test
    void generateApiKey_ShouldThrowUnauthorizedException_WhenUserIsUnauthorized() {
        // Arrange
        Long userId = 1L;
        doThrow(new UnauthorizedException("Unauthorized")).when(apiKeyService).generateApiKeyByUser(userId);

        // Act & Assert
        Exception exception = assertThrows(UnauthorizedException.class, () -> {
            apiKeyController.generateApiKey(userId);
        });
        assertEquals("Unauthorized", exception.getMessage()); // Verify exception message
    }

    // Unit Tests for getApiKey
    @Test
    void getApiKey_ShouldReturnOkResponse_WhenApiKeyExists() {
        // Arrange
        Long userId = 1L;
        ApiKeyDTO mockApiKeyDTO = new ApiKeyDTO("your-api-key-value", "user@example.com");
        when(apiKeyService.getApiKeyByUser(userId)).thenReturn(mockApiKeyDTO);

        // Act
        ResponseEntity<ApiKeyDTO> response = apiKeyController.getApiKey(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify response status
        assertEquals(mockApiKeyDTO, response.getBody()); // Verify response body
    }

    @Test
    void getApiKey_ShouldThrowApiKeyNotFoundException_WhenApiKeyDoesNotExist() {
        // Arrange
        Long userId = 1L;
        doThrow(new ApiKeyNotFoundException("API Key not found")).when(apiKeyService).getApiKeyByUser(userId);

        // Act & Assert
        Exception exception = assertThrows(ApiKeyNotFoundException.class, () -> {
            apiKeyController.getApiKey(userId);
        });
        assertEquals("API Key not found", exception.getMessage()); // Verify exception message
    }

    // Unit Tests for deleteApiKey
    @Test
    void deleteApiKey_ShouldReturnNoContentResponse_WhenApiKeyDeletedSuccessfully() {
        // Arrange
        Long userId = 1L;

        // Act
        ResponseEntity<Void> response = apiKeyController.deleteApiKey(userId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode()); // Verify response status
        verify(apiKeyService).deleteApiKeyByUser(userId); // Verify service interaction
    }

    @Test
    void deleteApiKey_ShouldThrowApiKeyNotFoundException_WhenApiKeyDoesNotExist() {
        // Arrange
        Long userId = 1L;
        doThrow(new ApiKeyNotFoundException("API Key not found")).when(apiKeyService).deleteApiKeyByUser(userId);

        // Act & Assert
        Exception exception = assertThrows(ApiKeyNotFoundException.class, () -> {
            apiKeyController.deleteApiKey(userId);
        });
        assertEquals("API Key not found", exception.getMessage()); // Verify exception message
    }

    // Unit Tests for approveApiKey
    @Test
    void approveApiKey_ShouldReturnOkResponse_WhenApiKeyApproved() {
        // Arrange
        Long apiKeyId = 1L;
        ApiKey mockApiKey = new ApiKey(); // Mock or instantiate your ApiKey object
        when(apiKeyService.approveApiKey(apiKeyId)).thenReturn(mockApiKey);

        // Act
        ResponseEntity<ApiKey> response = apiKeyController.approveApiKey(apiKeyId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify response status
        assertEquals(mockApiKey, response.getBody()); // Verify response body
    }

    @Test
    void approveApiKey_ShouldThrowApiKeyAlreadyApprovedException_WhenApiKeyAlreadyApproved() {
        // Arrange
        Long apiKeyId = 1L;
        doThrow(new ApiKeyAlreadyApprovedException("API Key already approved")).when(apiKeyService).approveApiKey(apiKeyId);

        // Act & Assert
        Exception exception = assertThrows(ApiKeyAlreadyApprovedException.class, () -> {
            apiKeyController.approveApiKey(apiKeyId);
        });
        assertEquals("API Key already approved", exception.getMessage()); // Verify exception message
    }

    // Unit Tests for listPendingApiKeys
    @Test
    void listPendingApiKeys_ShouldReturnOkResponse_WhenPendingApiKeysExist() {
        // Arrange
        List<ApiKey> mockPendingApiKeys = Collections.singletonList(new ApiKey());
        when(apiKeyService.listPendingApiKeys()).thenReturn(mockPendingApiKeys);

        // Act
        ResponseEntity<List<ApiKey>> response = apiKeyController.listPendingApiKeys();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify response status
        assertEquals(mockPendingApiKeys, response.getBody()); // Verify response body
    }

    // Unit Tests for listApprovedApiKeys
    @Test
    void listApprovedApiKeys_ShouldReturnOkResponse_WhenApprovedApiKeysExist() {
        // Arrange
        List<ApiKey> mockApprovedApiKeys = Collections.singletonList(new ApiKey());
        when(apiKeyService.listApprovedApiKeys()).thenReturn(mockApprovedApiKeys);

        // Act
        ResponseEntity<List<ApiKey>> response = apiKeyController.listApprovedApiKeys();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify response status
        assertEquals(mockApprovedApiKeys, response.getBody()); // Verify response body
    }

    // Unit Tests for Exception Handlers
    @Test
    void handleEmailAlreadyExists_ShouldReturnConflictResponse_WhenEmailAlreadyExists() {
        // Arrange
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already exists");
        
        // Act
        ResponseEntity<String> response = apiKeyController.new GlobalExceptionHandler().handleEmailAlreadyExists(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); // Verify response status
        assertEquals("Email already exists", response.getBody()); // Verify response body
    }

    @Test
    void handleApiKeyAlreadyExists_ShouldReturnConflictResponse_WhenApiKeyAlreadyExists() {
        // Arrange
        ApiKeyAlreadyExistsException ex = new ApiKeyAlreadyExistsException("API Key already exists");
        
        // Act
        ResponseEntity<String> response = apiKeyController.new GlobalExceptionHandler().handleApiKeyAlreadyExists(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); // Verify response status
        assertEquals("API Key already exists", response.getBody()); // Verify response body
    }

    @Test
    void handleUnauthorized_ShouldReturnUnauthorizedResponse_WhenUnauthorized() {
        // Arrange
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        
        // Act
        ResponseEntity<String> response = apiKeyController.new GlobalExceptionHandler().handleUnauthorizedException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()); // Verify response status
        assertEquals("Unauthorized", response.getBody()); // Verify response body
    }

    @Test
    void handleApiKeyNotFound_ShouldReturnNotFoundResponse_WhenApiKeyNotFound() {
        // Arrange
        ApiKeyNotFoundException ex = new ApiKeyNotFoundException("API Key not found");
        
        // Act
        ResponseEntity<String> response = apiKeyController.new GlobalExceptionHandler().handleApiKeyNotFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); // Verify response status
        assertEquals("API Key not found", response.getBody()); // Verify response body
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError_WhenExceptionThrown() {
        // Arrange
        Exception ex = new Exception("Some error");
        
        // Act
        ResponseEntity<String> response = apiKeyController.new GlobalExceptionHandler().handleGeneralException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()); // Verify response status
        assertEquals("An error occurred: Some error", response.getBody()); // Verify response body
    }


    @Test
void denyApiKey_ShouldReturnOkResponse_WhenApiKeyIsDenied() {
    // Arrange
    Long apiKeyId = 1L;
    ApiKey mockApiKey = new ApiKey(); // Mock o instancia tu objeto ApiKey
    when(apiKeyService.denyApiKey(apiKeyId)).thenReturn(mockApiKey);

    // Act
    ResponseEntity<ApiKey> response = apiKeyController.denyApiKey(apiKeyId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode()); // Verifica el estado de la respuesta
    assertEquals(mockApiKey, response.getBody()); // Verifica el cuerpo de la respuesta
}

@Test
void denyApiKey_ShouldThrowApiKeyNotFoundException_WhenApiKeyDoesNotExist() {
    // Arrange
    Long apiKeyId = 1L;
    doThrow(new ApiKeyNotFoundException("API Key not found")).when(apiKeyService).denyApiKey(apiKeyId);

    // Act & Assert
    Exception exception = assertThrows(ApiKeyNotFoundException.class, () -> {
        apiKeyController.denyApiKey(apiKeyId);
    });
    assertEquals("API Key not found", exception.getMessage()); // Verifica el mensaje de excepción
}

}
