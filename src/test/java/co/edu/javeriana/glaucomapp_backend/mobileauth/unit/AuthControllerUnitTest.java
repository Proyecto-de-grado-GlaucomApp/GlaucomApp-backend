package co.edu.javeriana.glaucomapp_backend.mobileauth.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;

import co.edu.javeriana.glaucomapp_backend.mobileauth.controller.AuthController;
import co.edu.javeriana.glaucomapp_backend.mobileauth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.mobileauth.model.RefreshTokenRequest;
import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.mobileauth.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletResponse;

public class AuthControllerUnitTest {
    @Mock
    private JwtUtil jwtUtil;


    @Mock
    private AuthServiceImpl authService;  // Use the concrete implementation

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        // Arrange
        MyUser user = new MyUser();

        // Act
        ResponseEntity<String> response = authController.createUser(user);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("User Created Successfully", response.getBody());
    }

    @Test
    public void testCreateUser_UsernameAlreadyInUse() throws Exception {
        // Arrange
        MyUser user = new MyUser();
        doThrow(new IllegalArgumentException("Username already in use")).when(authService).register(user);

        // Act
        ResponseEntity<String> response;
        try {
            response = authController.createUser(user);
        } catch (IllegalArgumentException e) {
            response = ResponseEntity.status(409).body("Error: Username already in use");
        }

        // Assert
        assertNotNull(response);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("Error: Username already in use", response.getBody());
    }

    @Test
    public void testCreateUser_EmptyFields() throws Exception {
        // Arrange
        MyUser user = new MyUser(); // Simulate empty fields
        doThrow(new IllegalArgumentException("Empty fields are not allowed")).when(authService).register(user);

        // Act
        ResponseEntity<String> response;
        try {
            response = authController.createUser(user);
        } catch (IllegalArgumentException e) {
            response = ResponseEntity.status(400).body("Error: Empty fields are not allowed");
        }

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Error: Empty fields are not allowed", response.getBody());
    }

    @Test
    public void testCreateUser_InternalServerError() throws Exception {
        // Arrange
        MyUser user = new MyUser();
        doThrow(new RuntimeException("Some other error")).when(authService).register(user);

        // Act
        ResponseEntity<String> response;
        try {
            response = authController.createUser(user);
        } catch (RuntimeException e) {
            response = ResponseEntity.status(500).body("An error occurred, please try later");
        }

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("An error occurred, please try later", response.getBody());
    }

    @Test
    public void testLogin_Success() throws Exception {
        // Arrange
        LogInForm loginForm = new LogInForm("username", "password");
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);

        // Act
        ResponseEntity<String> response = authController.authenticateAndSetToken(loginForm, responseMock);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Authentication successful", response.getBody());
    }

    @Test
    public void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        LogInForm loginForm = new LogInForm("username", "wrongpassword");
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        doThrow(new IllegalArgumentException("Invalid credentials")).when(authService).login(loginForm, responseMock);

        // Act
        ResponseEntity<String> response;
        try {
            response = authController.authenticateAndSetToken(loginForm, responseMock);
        } catch (IllegalArgumentException e) {
            response = ResponseEntity.status(401).body("Error: Unauthorized");
        }

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Error: Unauthorized", response.getBody());
    }

    @Test
    public void testLogout_Success() throws Exception {
        // Arrange
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);

        // Act
        ResponseEntity<String> response = authController.logout("Bearer someToken", responseMock);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Logout successful", response.getBody());
    }

    @Test
    public void testLogout_InternalServerError() throws Exception {
        // Arrange
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        doThrow(new RuntimeException("Some other error")).when(authService).logout("Bearer someToken", responseMock);

        // Act
        ResponseEntity<String> response;
        try {
            response = authController.logout("Bearer someToken", responseMock);
        } catch (Exception e) {
            response = ResponseEntity.status(500).body("An error occurred, please try later");
        }

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("An error occurred, please try later", response.getBody());
    }

    @Test
    public void testCloseAccount_Success() throws Exception {
        // Arrange
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);

        // Act
        ResponseEntity<String> response = authController.closeAccount("Bearer someToken", responseMock);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Account closed successfully", response.getBody());
    }

    @Test
    public void testCloseAccount_InternalServerError() throws Exception {
        // Arrange
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        doThrow(new RuntimeException("Some other error")).when(authService).closeAccount("Bearer someToken",
                responseMock);

        // Act
        ResponseEntity<String> response;
        try {
            response = authController.closeAccount("Bearer someToken", responseMock);
        } catch (Exception e) {
            response = ResponseEntity.status(500).body("An error occurred, please try later");
        }

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("An error occurred, please try later", response.getBody());
    }

    
    @Test
    public void testRefreshToken_Success() throws Exception {
        String refreshToken = "someRefreshToken";
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        String newAccessToken = "Token refreshed successfully";

        // Mocking the behavior of jwtUtil
        when(jwtUtil.refreshToken(refreshToken)).thenReturn(newAccessToken);

        // Act
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
        ResponseEntity<String> response = authController.refreshToken(refreshTokenRequest, responseMock);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(newAccessToken, response.getBody());
    }

    @Test
    public void testRefreshToken_InvalidToken() throws Exception {
        // Arrange
        String refreshToken = "invalidRefreshToken";
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        doThrow(new IllegalArgumentException("Invalid refresh token")).when(authService).refreshToken(refreshToken,
                responseMock);

        // Act
        ResponseEntity<String> response;
        try {
            RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
            response = authController.refreshToken(refreshTokenRequest, responseMock);
        } catch (IllegalArgumentException e) {
            response = ResponseEntity.status(401).body("Error: Invalid refresh token");
        }

        // Assert
        assertNotNull(response);
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Error: Invalid refresh token", response.getBody());
    }

    @Test
    public void testRefreshToken_InternalServerError() throws Exception {
        // Arrange
        String refreshToken = "someRefreshToken";
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        doThrow(new RuntimeException("Some internal error")).when(authService).refreshToken(refreshToken, responseMock);

        // Act
        ResponseEntity<String> response;
        try {
            RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
            response = authController.refreshToken(refreshTokenRequest, responseMock);
        } catch (RuntimeException e) {
            response = ResponseEntity.status(500).body("An error occurred, please try later");
        }

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("An error occurred, please try later", response.getBody());
    }

}
