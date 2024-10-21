package co.edu.javeriana.glaucomapp_backend.userapikey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class RoleRepositoryTest {

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes the mock objects
    }

    @Test
    void testFindByRole_Exists() {
        Role role = new Role();
        role.setRole(RoleEnum.USER);
        when(roleRepository.findByRole(RoleEnum.USER)).thenReturn(role);

        Role foundRole = roleRepository.findByRole(RoleEnum.USER);

        assertEquals(RoleEnum.USER, foundRole.getRole());
    }

    @Test
    void testFindByRole_NotExists() {
        when(roleRepository.findByRole(RoleEnum.ADMIN)).thenReturn(null);

        Role foundRole = roleRepository.findByRole(RoleEnum.ADMIN);

        assertNull(foundRole);
    }
}