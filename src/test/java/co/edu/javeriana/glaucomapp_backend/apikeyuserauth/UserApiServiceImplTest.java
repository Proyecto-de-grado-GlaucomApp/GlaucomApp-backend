package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
public class UserApiServiceImplTest {

    @Mock
    private UserApiRepository userApiRepository;

    @Mock
    private RoleRepository roleRepository;

    private UserApiServiceImpl userApiService;

    @BeforeEach
    public void setUp() {
        userApiService = new UserApiServiceImpl(userApiRepository, roleRepository);
    }

    @Test
    void testRegisterUser_Success() {
        String email = "email@example.com";
        UserApi savedUser = new UserApi(1L, email, "hashedPassword", "entity", "username", null);

        when(userApiRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userApiRepository.save(any(UserApi.class))).thenReturn(savedUser);

        UserApiDTO userApiDTO = new UserApiDTO(email, "entity", "plainPassword", "username");
        boolean result = userApiService.registerUser(userApiDTO);

        assertThat(result).isTrue();
        verify(userApiRepository, times(1)).save(any(UserApi.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        String email = "email1@example.com";
        when(userApiRepository.findByEmail(email)).thenReturn(Optional.of(new UserApi()));

        UserApiDTO userApiDTO = new UserApiDTO(email, "entity1", "plainPassword1", "username1");

        assertThatThrownBy(() -> userApiService.registerUser(userApiDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void testEditUser_Success() {
        Long userId = 1L;
        
        UserApi existingUser = new UserApi(1L, "email@example.com", "hashedPassword", "entity", "username", null);

        when(userApiRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userApiRepository.save(any(UserApi.class))).thenReturn(existingUser);

        UserApiDTO updatedUserApiDTO = new UserApiDTO("email@example.com", "newEntity", "newPlainPassword", "usernameUpdated");
        UserApi updatedUser = userApiService.editUser(userId, updatedUserApiDTO);

        assertThat(updatedUser.getContactName()).isEqualTo("usernameUpdated");
        assertThat(updatedUser.getEntity()).isEqualTo("newEntity");
    }

    @Test
    void testEditUser_EmailAlreadyExists() {
        UserApi existingUser = new UserApi(1L, "email@example.com", "hashedPassword", "entity", "username", null);
        when(userApiRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

        UserApiDTO updatedUserApiDTO = new UserApiDTO(existingUser.getEmail(), "newEntity", "newPlainPassword", "usernameUpdated");

        assertThatThrownBy(() -> userApiService.editUser(2L, updatedUserApiDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }
}
