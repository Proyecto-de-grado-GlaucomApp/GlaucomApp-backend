package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;

public class UserApiControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserApiServiceImpl userApiService;

    @InjectMocks
    private UserApiController userApiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        when(userApiService.registerUser(userApi)).thenReturn(true);

        ResponseEntity<String> response = userApiController.registerUser(userApi);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully.", response.getBody());
    }

    @Test
    void testRegisterUser_Failure() {
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        when(userApiService.registerUser(userApi)).thenReturn(false);

        ResponseEntity<String> response = userApiController.registerUser(userApi);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error registering the user.", response.getBody());
    }

    @Test
    void testLogin_Success() {
        LoginRequestDTO loginUser = new LoginRequestDTO("email@example.com", "password");
        Role role = new Role();
        role.setRole(RoleEnum.USER);
        UserApi user = new UserApi(1L, "email@example.com", "password", "entity", "username", role);
        when(userApiService.findUserByEmail(loginUser.email())).thenReturn(Optional.of(user));
        when(userApiService.verifyPassword(user, loginUser.password())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().getRole().toString())).thenReturn("token");

        ResponseEntity<?> response = userApiController.login(loginUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token", response.getBody());
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequestDTO loginUser = new LoginRequestDTO("email@example.com", "wrongpassword");
        Role role = new Role();
        role.setRole(RoleEnum.USER);
        UserApi user = new UserApi(1L, "email@example.com", "password", "entity", "username", role); // Correct password is "password"

        // Test case for user not found
        when(userApiService.findUserByEmail(loginUser.email())).thenReturn(Optional.empty());
        ResponseEntity<?> response = userApiController.login(loginUser);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Invalid email or password."), response.getBody());

        // Test case for wrong password
        when(userApiService.findUserByEmail(loginUser.email())).thenReturn(Optional.of(user));
        when(userApiService.verifyPassword(user, loginUser.password())).thenReturn(false); // Simulate incorrect password
        response = userApiController.login(loginUser);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Invalid email or password."), response.getBody());
    }

    @Test
    void testEditUser_Success() {
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        Role role = new Role();
        role.setRole(RoleEnum.USER);
        UserApi updatedUser = new UserApi(1L, "email@example.com", "password", "entity", "username", role);
        when(userApiService.editUser(1L, userApi)).thenReturn(updatedUser);

        UserApi response = userApiController.editUser(1L, userApi);

        assertEquals(updatedUser, response);
    }

    @Test
    void testHandleEmailAlreadyExistsException() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already exists.");
        ResponseEntity<String> response = userApiController.new GlobalExceptionHandler().handleEmailAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already exists.", response.getBody());
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new Exception("General error.");
        ResponseEntity<String> response = userApiController.new GlobalExceptionHandler().handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: General error.", response.getBody());
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized access.");
        ResponseEntity<String> response = userApiController.new GlobalExceptionHandler().handleUnauthorizedException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized access.", response.getBody());
    }
}
