/**
 * GlobalExceptionHandler is a centralized exception handling class that uses
 * Spring's @ControllerAdvice to handle exceptions thrown by the application.
 * It provides specific handlers for different types of exceptions and returns
 * appropriate HTTP status codes and error messages.
 * 
 * Handled exceptions:
 * - UsernameNotFoundException: Returns a 404 NOT FOUND status with the error message.
 * - UnauthorizedException: Returns a 401 UNAUTHORIZED status with a generic unauthorized message.
 * - IllegalArgumentException: Returns a 400 BAD REQUEST status with the error message.
 * - Exception: Returns a 500 INTERNAL SERVER ERROR status with a generic error message.
 */
package co.edu.javeriana.glaucomapp_backend.auth.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFound(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Unauthorized");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred, please try later");
    }
}

