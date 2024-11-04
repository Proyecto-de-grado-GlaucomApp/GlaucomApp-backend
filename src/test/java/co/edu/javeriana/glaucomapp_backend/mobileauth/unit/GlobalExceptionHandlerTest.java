package co.edu.javeriana.glaucomapp_backend.mobileauth.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;
import co.edu.javeriana.glaucomapp_backend.mobileauth.Exception.GlobalExceptionHandler;



public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleUsernameNotFound() {
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");
        ResponseEntity<String> response = globalExceptionHandler.handleUsernameNotFound(exception);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Error: User not found", response.getBody());
    }

    @Test
    public void testHandleUnauthorized() {
        UnauthorizedException exception = mock(UnauthorizedException.class);
        ResponseEntity<String> response = globalExceptionHandler.handleUnauthorized(exception);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Error: Unauthorized", response.getBody());
    }

    @Test
    public void testHandleIllegalArgument() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgument(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: Invalid argument", response.getBody());
    }

    @Test
    public void testHandleIllegalArgumentUsernameInUse() {
        IllegalArgumentException exception = new IllegalArgumentException("Username already in use");
        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgument(exception);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Error: Username already in use", response.getBody());
    }

    @Test
    public void testHandleGeneralException() {
        Exception exception = new Exception("General error");
        ResponseEntity<String> response = globalExceptionHandler.handleGeneralException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred, please try later", response.getBody());
    }
}