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
        MockitoAnnotations.openMocks(this);
    }

    // Unit Tests for generateApiKey
    @Test
    void generateApiKey_ShouldReturnResponse_WhenApiKeyIsGeneratedOrUnauthorized() {
        Long userId = 1L;
        ApiKey mockApiKey = new ApiKey();
        when(apiKeyService.generateApiKeyByUser(userId)).thenReturn(mockApiKey);
        
        // Act
        ResponseEntity<ApiKey> response = apiKeyController.generateApiKey(userId);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockApiKey, response.getBody());

        // Act & Assert for UnauthorizedException
        doThrow(new UnauthorizedException("Unauthorized")).when(apiKeyService).generateApiKeyByUser(userId);
        Exception exception = assertThrows(UnauthorizedException.class, () -> {
            apiKeyController.generateApiKey(userId);
        });
        assertEquals("Unauthorized", exception.getMessage());
    }

    // Unit Tests for getApiKey
    @Test
    void getApiKey_ShouldReturnResponse_WhenApiKeyExistsOrNotFound() {
        Long userId = 1L;
        ApiKeyDTO mockApiKeyDTO = new ApiKeyDTO("your-api-key-value", "user@example.com");
        when(apiKeyService.getApiKeyByUser(userId)).thenReturn(mockApiKeyDTO);
        
        // Act
        ResponseEntity<ApiKeyDTO> response = apiKeyController.getApiKey(userId);

        // Assert for existing API key
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockApiKeyDTO, response.getBody());

        // Act & Assert for ApiKeyNotFoundException
        doThrow(new ApiKeyNotFoundException("API Key not found")).when(apiKeyService).getApiKeyByUser(userId);
        Exception exception = assertThrows(ApiKeyNotFoundException.class, () -> {
            apiKeyController.getApiKey(userId);
        });
        assertEquals("API Key not found", exception.getMessage());
    }

    // Unit Tests for deleteApiKey
    @Test
    void deleteApiKey_ShouldReturnResponse_WhenApiKeyDeletedOrNotFound() {
        Long userId = 1L;

        // Act
        ResponseEntity<Void> response = apiKeyController.deleteApiKey(userId);

        // Assert for successful deletion
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(apiKeyService).deleteApiKeyByUser(userId);

        // Act & Assert for ApiKeyNotFoundException
        doThrow(new ApiKeyNotFoundException("API Key not found")).when(apiKeyService).deleteApiKeyByUser(userId);
        Exception exception = assertThrows(ApiKeyNotFoundException.class, () -> {
            apiKeyController.deleteApiKey(userId);
        });
        assertEquals("API Key not found", exception.getMessage());
    }

    // Unit Tests for approveApiKey
    @Test
    void approveApiKey_ShouldReturnResponse_WhenApiKeyApprovedOrAlreadyApproved() {
        Long apiKeyId = 1L;
        ApiKey mockApiKey = new ApiKey();
        when(apiKeyService.approveApiKey(apiKeyId)).thenReturn(mockApiKey);
        
        // Act
        ResponseEntity<ApiKey> response = apiKeyController.approveApiKey(apiKeyId);

        // Assert for successful approval
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockApiKey, response.getBody());

        // Act & Assert for ApiKeyAlreadyApprovedException
        doThrow(new ApiKeyAlreadyApprovedException("API Key already approved")).when(apiKeyService).approveApiKey(apiKeyId);
        Exception exception = assertThrows(ApiKeyAlreadyApprovedException.class, () -> {
            apiKeyController.approveApiKey(apiKeyId);
        });
        assertEquals("API Key already approved", exception.getMessage());
    }

    // Unit Tests for listPendingApiKeys
    @Test
    void listPendingApiKeys_ShouldReturnOkResponse_WhenPendingApiKeysExist() {
        List<ApiKey> mockPendingApiKeys = Collections.singletonList(new ApiKey());
        when(apiKeyService.listPendingApiKeys()).thenReturn(mockPendingApiKeys);

        ResponseEntity<List<ApiKey>> response = apiKeyController.listPendingApiKeys();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPendingApiKeys, response.getBody());
    }

    // Unit Tests for listApprovedApiKeys
    @Test
    void listApprovedApiKeys_ShouldReturnOkResponse_WhenApprovedApiKeysExist() {
        List<ApiKey> mockApprovedApiKeys = Collections.singletonList(new ApiKey());
        when(apiKeyService.listApprovedApiKeys()).thenReturn(mockApprovedApiKeys);

        ResponseEntity<List<ApiKey>> response = apiKeyController.listApprovedApiKeys();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockApprovedApiKeys, response.getBody());
    }

    // Unit Tests for Exception Handlers
    @Test
    void handleException_ShouldReturnExpectedResponse_WhenDifferentExceptionsAreThrown() {
        EmailAlreadyExistsException emailException = new EmailAlreadyExistsException("Email already exists");
        ApiKeyAlreadyExistsException apiKeyException = new ApiKeyAlreadyExistsException("API Key already exists");
        UnauthorizedException unauthorizedException = new UnauthorizedException("Unauthorized");
        ApiKeyNotFoundException notFoundException = new ApiKeyNotFoundException("API Key not found");
        Exception generalException = new Exception("Some error");

        // Act & Assert for EmailAlreadyExistsException
        ResponseEntity<String> response = apiKeyController.new GlobalExceptionHandler().handleEmailAlreadyExists(emailException);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already exists", response.getBody());

        // Act & Assert for ApiKeyAlreadyExistsException
        response = apiKeyController.new GlobalExceptionHandler().handleApiKeyAlreadyExists(apiKeyException);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("API Key already exists", response.getBody());

        // Act & Assert for UnauthorizedException
        response = apiKeyController.new GlobalExceptionHandler().handleUnauthorizedException(unauthorizedException);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody());

        // Act & Assert for ApiKeyNotFoundException
        response = apiKeyController.new GlobalExceptionHandler().handleApiKeyNotFoundException(notFoundException);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("API Key not found", response.getBody());

        // Act & Assert for general exception
        response = apiKeyController.new GlobalExceptionHandler().handleGeneralException(generalException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: Some error", response.getBody());
    }

    @Test
    void denyApiKey_ShouldReturnResponse_WhenApiKeyIsDeniedOrNotFound() {
        Long apiKeyId = 1L;
        ApiKey mockApiKey = new ApiKey();
        when(apiKeyService.denyApiKey(apiKeyId)).thenReturn(mockApiKey);
        
        // Act
        ResponseEntity<ApiKey> response = apiKeyController.denyApiKey(apiKeyId);

        // Assert for successful denial
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockApiKey, response.getBody());

        // Act & Assert for ApiKeyNotFoundException
        doThrow(new ApiKeyNotFoundException("API Key not found")).when(apiKeyService).denyApiKey(apiKeyId);
        Exception exception = assertThrows(ApiKeyNotFoundException.class, () -> {
            apiKeyController.denyApiKey(apiKeyId);
        });
        assertEquals("API Key not found", exception.getMessage());
    }
}
