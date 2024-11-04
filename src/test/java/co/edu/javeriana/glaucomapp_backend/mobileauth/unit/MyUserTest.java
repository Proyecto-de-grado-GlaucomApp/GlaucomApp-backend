package co.edu.javeriana.glaucomapp_backend.mobileauth.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUser;




public class MyUserTest {

    @Test
    public void testMyUserBuilder() {
        UUID id = UUID.randomUUID();
        MyUser user = MyUser.builder()
                .id(id)
                .username("testuser")
                .password("password")
                .name("Test User")
                .role("USER")
                .build();

        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals("USER", user.getRole());
    }

    @Test
    public void testMyUserNoArgsConstructor() {
        MyUser user = new MyUser();

        assertNotNull(user);
    }

    @Test
    public void testMyUserAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        MyUser user = new MyUser(id, "testuser", "password", "Test User", "USER");

        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals("USER", user.getRole());
    }

    @Test
    public void testToString() {
        UUID id = UUID.randomUUID();
        MyUser user = new MyUser(id, "testuser", "password", "Test User", "USER");

        String expected = "MyUser{id=" + id + ", username='testuser', name='Test User', role='USER'}";
        assertEquals(expected, user.toString());
    }
}