package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
public class UserApiServiceImplUnitTest {

    @Mock
    private UserApiRepository userApiRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserApiServiceImpl userApiService;

    @BeforeEach
    public void setUp() {
        userApiService = new UserApiServiceImpl(userApiRepository, roleRepository);
    }


    @Test
    void testRegisterUser_Success() {
        
        when(userApiRepository.findByEmail("email@example.com")).thenReturn(Optional.empty());
    
           
        UserApi savedUser = new UserApi();
        savedUser.setId(1L); // Asigna un ID simulado para verificar que el guardado fue exitoso
        savedUser.setEmail("email@example.com");
        savedUser.setContactName("username");
        savedUser.setEntity("entity");
        savedUser.setHashedPassword("hashedPassword");
    
        when(userApiRepository.save(any(UserApi.class))).thenReturn(savedUser);
    
        // Llama al método que se va a probar
        UserApiDTO userApiDTO = new UserApiDTO("email@example.com", "entity", "plainPassword", "username");
        boolean result = userApiService.registerUser(userApiDTO);
    
        // Verifica que el resultado sea true
        assertThat(result).isTrue();
    
        // Verifica que se haya llamado al método save en userApiRepository
        verify(userApiRepository, times(1)).save(any(UserApi.class));
    }
    


    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Configura el mock para devolver un usuario existente
        when(userApiRepository.findByEmail("email1@example.com")).thenReturn(Optional.of(new UserApi()));

        UserApiDTO userApiDTO1 = new UserApiDTO("email1@example.com", "entity1", "plainPassword1", "username1");

        assertThatThrownBy(() -> userApiService.registerUser(userApiDTO1))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void testEditUser_Success() {
        UserApi existingUser = new UserApi();
        existingUser.setId(1L);
        existingUser.setEmail("email@example.com");

        when(userApiRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userApiRepository.findByEmail("email@example.com")).thenReturn(Optional.of(existingUser));
        when(userApiRepository.save(any(UserApi.class))).thenReturn(existingUser);

        UserApiDTO updatedUserApiDTO = new UserApiDTO("email@example.com", "newEntity", "newPlainPassword", "usernameUpdated");

        UserApi updatedUser = userApiService.editUser(1L, updatedUserApiDTO);

        assertThat(updatedUser.getContactName()).isEqualTo("usernameUpdated");
        assertThat(updatedUser.getEntity()).isEqualTo("newEntity");
    }

    @Test
    void testEditUser_EmailAlreadyExists() {
        UserApi existingUser1 = new UserApi();
        existingUser1.setId(1L);
        existingUser1.setEmail("email1@example.com");
    
        UserApi existingUser2 = new UserApi();
        existingUser2.setId(2L);
        existingUser2.setEmail("email2@example.com");
    
        when(userApiRepository.findByEmail("email1@example.com")).thenReturn(Optional.of(existingUser1)); // Se espera que este correo ya esté en uso
    
        UserApiDTO updatedUserApiDTO = new UserApiDTO("email1@example.com", "newEntity", "newPlainPassword", "usernameUpdated");
    
        // Asegúrate de que se lanza la excepción
        assertThatThrownBy(() -> userApiService.editUser(2L, updatedUserApiDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }
    
    
}
