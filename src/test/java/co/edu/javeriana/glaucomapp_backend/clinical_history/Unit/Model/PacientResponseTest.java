package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;


public class PacientResponseTest {

    @Test
    public void testPacientResponseCreation() {
        UUID id = UUID.randomUUID();
        String name = "John Doe";
        String cedula = "123456789";

        PacientResponse pacientResponse = new PacientResponse(id, name, cedula);

        assertNotNull(pacientResponse);
        assertEquals(id, pacientResponse.PacinetId());
        assertEquals(name, pacientResponse.name());
        assertEquals(cedula, pacientResponse.cedula());
    }
}