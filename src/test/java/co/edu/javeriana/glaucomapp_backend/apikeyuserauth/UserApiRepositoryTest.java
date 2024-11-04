package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@DataJpaTest

@ActiveProfiles("test")
public class UserApiRepositoryTest {

    @Autowired
    private UserApiRepository userApiRepository;

    @BeforeEach
    void setUp() {
        // Configurar datos iniciales para las pruebas
        UserApi user = new UserApi();
        user.setEmail("test@example.com");
        user.setHashedPassword("password123");
        userApiRepository.save(user);
    }

    @Test
    void testFindByEmail_ReturnsUserApi_WhenEmailExists() {
        Optional<UserApi> foundUser = userApiRepository.findByEmail("test@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByEmail_ReturnsEmpty_WhenEmailDoesNotExist() {
        Optional<UserApi> foundUser = userApiRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isNotPresent();
    }
}