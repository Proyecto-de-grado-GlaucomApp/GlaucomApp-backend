package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;

public class PacientTest {

    private Pacient pacient;

    @BeforeEach
    public void setUp() {
        pacient = Pacient.builder()
                .id(UUID.randomUUID())
                .cedula("123456789")
                .name("John Doe")
                .doctorId(UUID.randomUUID())
                .build();
    }

    @Test
    public void testPacientNotNull() {
        assertNotNull(pacient);
    }

    @Test
    public void testPacientId() {
        assertNotNull(pacient.getId());
    }

    @Test
    public void testPacientCedula() {
        assertEquals("123456789", pacient.getCedula());
    }

    @Test
    public void testPacientName() {
        assertEquals("John Doe", pacient.getName());
    }

    @Test
    public void testPacientDoctorId() {
        assertNotNull(pacient.getDoctorId());
    }

    @Test
    public void testPacientToString() {
        String expected = "Pacient{id=" + pacient.getId() + ", cedula='123456789', name='John Doe'}";
        assertEquals(expected, pacient.toString());
    }
}