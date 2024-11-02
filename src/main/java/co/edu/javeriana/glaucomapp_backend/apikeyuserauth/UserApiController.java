package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;
import lombok.AllArgsConstructor;

/**
 * Controller for managing user API operations such as registration, login, and editing user information.
 * 
 * Provides endpoints for user-related actions and handles exceptions for specific cases.
 */
@RestController
@RequestMapping("/api/v1/api-key/auth") // Versioning added to the API
@AllArgsConstructor
public class UserApiController {

    private final JwtUtil jwtUtil;
    private final UserApiServiceImpl userApiService; // Suponiendo que también usas este servicio



    /**
     * Registers a new user.
     *
     * @param userApi the user information to register
     * @return ResponseEntity with a success message or error status
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserApiDTO userApi) {
        boolean isRegistered = userApiService.registerUser(userApi);

        if (isRegistered) {
            return ResponseEntity.status(HttpStatus.CREATED) // 201 Created
                    .body("User registered successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                    .body("Error registering the user.");
        }
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginUser the login credentials
     * @return ResponseEntity containing the JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginUser) {
            Optional<UserApi> optionalUser = userApiService.findUserByEmail(loginUser.email());

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 Unauthorized
                        .body(Map.of("error", "Invalid email or password."));
            }

            UserApi user = optionalUser.get();

            // Verify if the password is correct
            if (!userApiService.verifyPassword(user, loginUser.password())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 Unauthorized
                        .body(Map.of("error", "Invalid email or password."));
            }

            // Generate the JWT
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().getRole().toString());
            return ResponseEntity.ok().body(token);

    }

    /**
     * Edits user information.
     *
     * @param userId the ID of the user to edit
     * @param userApi the new user information
     * @return the updated UserApi object
     */
    @PutMapping("/{userId}/edit")
    public UserApi editUser(@PathVariable Long userId, @RequestBody UserApiDTO userApi) {
        return userApiService.editUser(userId, userApi);
    }

    /**
     * Global exception handler for handling specific exceptions.
     */
    @RestControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
                    .body(ex.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleGeneralException(Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .body("An error occurred: " + ex.getMessage());
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 Unauthorized
                    .body(ex.getMessage());
        }
    }
}
