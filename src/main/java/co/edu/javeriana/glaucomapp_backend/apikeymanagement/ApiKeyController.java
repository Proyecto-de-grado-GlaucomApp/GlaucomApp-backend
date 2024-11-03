package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.javeriana.glaucomapp_backend.apikeymanagement.exposed.ApiKeyDTO;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyApprovedException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.ApiKeyNotFoundException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;

/**
 * Controller for managing API key operations such as generating and invalidating API keys.
 * <p>
 * This class provides endpoints for clients to generate new API keys, retrieve existing keys,
 * approve pending keys, and delete keys for specific users.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/api-keys") // Base path for API key operations with versioning
public class ApiKeyController {

    private final ApiKeyInternalService apiKeyService;

    /**
     * Constructs an instance of {@link ApiKeyController}.
     *
     * @param apiKeyService the service to manage API keys
     */
    public ApiKeyController(ApiKeyInternalService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Endpoint to generate a new API key for a specific user.
     *
     * @param userId the ID of the user for whom to generate an API key
     * @return ResponseEntity containing the generated API key with HTTP status 201 (Created)
     */
    @PostMapping("/users/{userId}")
    @ValidateJwtId(paramName = "userId") // Validate JWT token with the user ID
    public ResponseEntity<ApiKey> generateApiKey(@PathVariable Long userId) {
        ApiKey apiKey = apiKeyService.generateApiKeyByUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKey); // Return 201 Created
    }

    /**
     * Endpoint to retrieve the API key for a specific user.
     *
     * @param userId the ID of the user whose API key is to be retrieved
     * @return ResponseEntity containing the user's API key information with HTTP status 200 (OK)
     */
    @GetMapping("/users/{userId}")
    @ValidateJwtId(paramName = "userId")
    public ResponseEntity<ApiKeyDTO> getApiKey(@PathVariable Long userId) {
        System.out.println("getApiKey:  " + userId);
        ApiKeyDTO apiKeyDTO = apiKeyService.getApiKeyByUser(userId);
        System.out.println("getApiKey:  " + apiKeyDTO);
        return ResponseEntity.ok(apiKeyDTO); // Return 200 OK
    }

    /**
     * Endpoint to delete the API key for a specific user.
     *
     * @param userId the ID of the user whose API key is to be deleted
     * @return ResponseEntity with HTTP status 204 (No Content) if successful
     */
    @DeleteMapping("/users/{userId}")
    @ValidateJwtId(paramName = "userId")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long userId) {
        apiKeyService.deleteApiKeyByUser(userId);
        return ResponseEntity.noContent().build(); // Return 204 No Content
    }

    /**
     * Endpoint to approve an API key.
     *
     * @param apiKeyId the ID of the API key to be approved
     * @return ResponseEntity containing the approved API key with HTTP status 200 (OK)
     */
    @PutMapping("/{apiKeyId}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiKey> approveApiKey(@PathVariable Long apiKeyId) {
        ApiKey apiKey = apiKeyService.approveApiKey(apiKeyId);
        return ResponseEntity.ok(apiKey); // Return 200 OK
    }


    @PutMapping("/{apiKeyId}/deny")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiKey> denyApiKey(@PathVariable Long apiKeyId) {
        ApiKey apiKey = apiKeyService.denyApiKey(apiKeyId);
        return ResponseEntity.ok(apiKey); // Return 200 OK
    }
    /**
     * Endpoint to list all pending API keys.
     *
     * @return ResponseEntity containing a list of pending API keys with HTTP status 200 (OK)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ApiKey>> listPendingApiKeys() {
        List<ApiKey> pendingApiKeys = apiKeyService.listPendingApiKeys();
        return ResponseEntity.ok(pendingApiKeys); // Return 200 OK
    }

    /**
     * Endpoint to list all approved API keys.
     *
     * @return ResponseEntity containing a list of approved API keys with HTTP status 200 (OK)
     */
    @GetMapping("/approved")
    //Rol Admin
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ApiKey>> listApprovedApiKeys() {
        List<ApiKey> approvedApiKeys = apiKeyService.listApprovedApiKeys();
        return ResponseEntity.ok(approvedApiKeys); // Return 200 OK
    }

    /**
     * Global exception handler for managing exceptions throughout the API.
     */
    @RestControllerAdvice
    public class GlobalExceptionHandler {

        /**
         * Handles EmailAlreadyExistsException and returns a conflict status.
         *
         * @param ex the exception to handle
         * @return ResponseEntity with HTTP status 409 (Conflict) and the exception message
         */
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }

        /**
         * Handles any general exception and returns an internal server error status.
         *
         * @param ex the exception to handle
         * @return ResponseEntity with HTTP status 500 (Internal Server Error) and a generic message
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleGeneralException(Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + ex.getMessage());
        }

        /**
         * Handles ApiKeyAlreadyExistsException and returns a conflict status.
         *
         * @param ex the exception to handle
         * @return ResponseEntity with HTTP status 409 (Conflict) and the exception message
         */
        @ExceptionHandler(ApiKeyAlreadyExistsException.class)
        public ResponseEntity<String> handleApiKeyAlreadyExists(ApiKeyAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }

        /**
         * Handles UnauthorizedException and returns an unauthorized status.
         *
         * @param ex the exception to handle
         * @return ResponseEntity with HTTP status 401 (Unauthorized) and the exception message
         */
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }

        /**
         * Handles ApiKeyNotFoundException and returns a not found status.
         *
         * @param ex the exception to handle
         * @return ResponseEntity with HTTP status 404 (Not Found) and the exception message
         */
        @ExceptionHandler(ApiKeyNotFoundException.class)
        public ResponseEntity<String> handleApiKeyNotFoundException(ApiKeyNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }

        /**
         * Handles ApiKeyAlreadyApprovedException and returns a conflict status.
         *
         * @param ex the exception to handle
         * @return ResponseEntity with HTTP status 409 (Conflict) and the exception message
         */
        @ExceptionHandler(ApiKeyAlreadyApprovedException.class)
        public ResponseEntity<String> handleApiKeyAlreadyApproved(ApiKeyAlreadyApprovedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }
}
