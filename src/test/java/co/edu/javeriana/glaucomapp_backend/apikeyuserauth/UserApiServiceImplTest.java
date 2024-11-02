package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;


import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;

public class UserApiServiceImplTest {

    @Mock
    private UserApiRepository userApiRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserApiServiceImpl userApiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        Role role = new Role();
        role.setRole(RoleEnum.USER);
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        when(userApiRepository.findByEmail(userApi.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userApi.plainPassword())).thenReturn("hashedPassword");
        when(roleRepository.findByRole(RoleEnum.USER)).thenReturn(role);
        when(userApiRepository.save(any(UserApi.class))).thenReturn(new UserApi());

        boolean result = userApiService.registerUser(userApi);

        assertTrue(result);
        verify(userApiRepository).save(any(UserApi.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        when(userApiRepository.findByEmail(userApi.email())).thenReturn(Optional.of(new UserApi()));

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> userApiService.registerUser(userApi));
        assertNotNull(exception);
    }

    @Test
    void testVerifyPassword_Matching() {
        UserApi userApi = new UserApi();
        String rawPassword = "admin"; 
        String hashedPassword = "$2a$10$G1RGmOFB0cZazlqCbPuGZestJzldh5r3k1DSVqob9dVmMsm545yl.";

        userApi.setHashedPassword(hashedPassword);
        
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(passwordEncoder.matches(rawPassword, userApi.getHashedPassword())).thenReturn(true);

        boolean result = userApiService.verifyPassword(userApi, rawPassword);
    
        assertTrue(result);
    }

    @Test
    void testVerifyPassword_NonMatching() {
        UserApi userApi = new UserApi();
        userApi.setHashedPassword("hashedPassword");
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(false);

        boolean result = userApiService.verifyPassword(userApi, "password");

        assertFalse(result);
    }

    @Test
    void testFindUserByEmail_UserFound() {
        UserApi userApi = new UserApi();
        when(userApiRepository.findByEmail("email@example.com")).thenReturn(Optional.of(userApi));

        Optional<UserApi> result = userApiService.findUserByEmail("email@example.com");

        assertTrue(result.isPresent());
        assertEquals(userApi, result.get());
    }

    @Test
    void testFindUserByEmail_UserNotFound() {
        when(userApiRepository.findByEmail("email@example.com")).thenReturn(Optional.empty());

        Optional<UserApi> result = userApiService.findUserByEmail("email@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testEditUser_Success() {
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        UserApi existingUser = new UserApi();
        existingUser.setId(1L);
        when(userApiRepository.findByEmail(userApi.email())).thenReturn(Optional.empty());
        when(userApiRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(userApi.plainPassword())).thenReturn("hashedPassword");
        when(userApiRepository.save(any(UserApi.class))).thenReturn(existingUser);

        UserApi result = userApiService.editUser(1L, userApi);

        assertEquals(existingUser, result);
        verify(userApiRepository).save(any(UserApi.class));
    }

    @Test
    void testEditUser_EmailAlreadyExists() {
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        UserApi existingUser = new UserApi();
        existingUser.setId(2L);
        when(userApiRepository.findByEmail(userApi.email())).thenReturn(Optional.of(existingUser));

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> userApiService.editUser(1L, userApi));
        assertNotNull(exception);
    }

    @Test
    void testRegisterUser_SaveReturnsNull() {
        UserApiDTO userApi = new UserApiDTO("username", "email@example.com", "password", "role");
        when(userApiRepository.findByEmail(userApi.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userApi.plainPassword())).thenReturn("hashedPassword");
        when(roleRepository.findByRole(RoleEnum.USER)).thenReturn(new Role());
        when(userApiRepository.save(any(UserApi.class))).thenReturn(null); // Simula que el guardado falla

        boolean result = userApiService.registerUser(userApi);

        assertFalse(result);
    }
}
