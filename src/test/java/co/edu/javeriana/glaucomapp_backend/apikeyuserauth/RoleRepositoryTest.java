package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class RoleRepositoryTest {

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes the mock objects
    }

    @Test
    void testFindByRole() {
        Role userRole = new Role();
        userRole.setRole(RoleEnum.USER);
        when(roleRepository.findByRole(RoleEnum.USER)).thenReturn(userRole);
        
        // Test for existing role
        Role foundUserRole = roleRepository.findByRole(RoleEnum.USER);
        assertEquals(RoleEnum.USER, foundUserRole.getRole(), "Expected role should be USER");

        // Test for non-existing role
        when(roleRepository.findByRole(RoleEnum.ADMIN)).thenReturn(null);
        Role foundAdminRole = roleRepository.findByRole(RoleEnum.ADMIN);
        assertNull(foundAdminRole, "Expected role should be null for ADMIN");
    }
}
