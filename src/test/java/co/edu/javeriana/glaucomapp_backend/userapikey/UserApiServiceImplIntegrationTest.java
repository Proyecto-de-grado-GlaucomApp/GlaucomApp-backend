package co.edu.javeriana.glaucomapp_backend.userapikey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;


@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class UserApiServiceImplIntegrationTest {

    @Autowired
    private UserApiRepository userApiRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserApiServiceImpl userApiService;

    @BeforeEach
    public void setUp() {
        userApiService = new UserApiServiceImpl(userApiRepository, roleRepository);
    }

    @Test
    void testRegisterUser_Success() {
        UserApiDTO userApiDTO = new UserApiDTO( "email@example.com","entity", "plainPassword", "username");

        boolean result = userApiService.registerUser(userApiDTO);

        assertThat(result).isTrue();
        Optional<UserApi> userOptional = userApiRepository.findByEmail("email@example.com");
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getEmail()).isEqualTo("email@example.com");
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        UserApiDTO userApiDTO1 = new UserApiDTO( "email1@example.com","entity1", "plainPassword1", "username1");
        UserApiDTO userApiDTO2 = new UserApiDTO( "email1@example.com","entity2", "plainPassword2", "username2");

        userApiService.registerUser(userApiDTO1);

        assertThatThrownBy(() -> userApiService.registerUser(userApiDTO2))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void testEditUser_Success() {
        UserApiDTO userApiDTO = new UserApiDTO( "email@example.com","entity", "plainPassword", "username");
        userApiService.registerUser(userApiDTO);
        
        UserApiDTO updatedUserApiDTO = new UserApiDTO("email@example.com", "newEntity", "newPlainPassword", "usernameUpdated");
        UserApi user = userApiRepository.findByEmail("email@example.com").get();

        UserApi updatedUser = userApiService.editUser(user.getId(), updatedUserApiDTO);

        assertThat(updatedUser.getContactName()).isEqualTo("usernameUpdated");
        assertThat(updatedUser.getEntity()).isEqualTo("newEntity");
    }

    @Test
    void testEditUser_EmailAlreadyExists() {
        UserApiDTO userApiDTO1 = new UserApiDTO( "email1@example.com","entity1", "plainPassword1", "username1");
        UserApiDTO userApiDTO2 = new UserApiDTO( "email2@example.com","entity2", "plainPassword2", "username2");
        userApiService.registerUser(userApiDTO1);
        userApiService.registerUser(userApiDTO2);

        UserApi user2 = userApiRepository.findByEmail("email2@example.com").get();

        UserApiDTO updatedUserApiDTO = new UserApiDTO("email1@example.com", "newEntity", "newPlainPassword", "usernameUpdated");

        assertThatThrownBy(() -> userApiService.editUser(user2.getId(), updatedUserApiDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");
    }
}
