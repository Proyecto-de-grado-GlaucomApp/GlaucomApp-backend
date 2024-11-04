package co.edu.javeriana.glaucomapp_backend.mobileauth.unit;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.mobileauth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.mobileauth.service.OphtalmologistEventListerner;

public class OphtalmologistEventListernerTest {

    @Mock
    private MyUserRepository userRepository;

    @InjectMocks
    private OphtalmologistEventListerner ophtalEventHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Object invokePrivateMethod(String methodName, Object instance, Object... args) throws Exception {
        Method method = OphtalmologistEventListerner.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return method.invoke(instance, args);
    }

    @Test
    public void testOn_ValidOphtalId() throws Exception {
        UUID validUUID = UUID.randomUUID();
        when(userRepository.findById(validUUID)).thenReturn(Optional.of(new MyUser())); // Mocking a valid user

        invokePrivateMethod("on", ophtalEventHandler, validUUID.toString());
    }

    @Test
    public void testOn_InvalidOphtalId() {
        UUID invalidUUID = UUID.randomUUID();
        when(userRepository.findById(invalidUUID)).thenReturn(Optional.empty()); // Mocking an invalid user

        Exception exception = assertThrows(Exception.class, () -> {
            invokePrivateMethod("on", ophtalEventHandler, invalidUUID.toString());
        });

        // Check if the cause of the exception is an IllegalArgumentException
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof IllegalArgumentException);
        assertEquals("Invalid Ophthalmologist ID", cause.getMessage());
    }

    @Test
    public void testOn_MalformedOphtalId() {
        String malformedId = "invalid-uuid";

        Exception exception = assertThrows(Exception.class, () -> {
            invokePrivateMethod("on", ophtalEventHandler, malformedId);
        });

        // Now check the cause of the exception
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Invalid UUID string: invalid-uuid", exception.getCause().getMessage());
    }

}
