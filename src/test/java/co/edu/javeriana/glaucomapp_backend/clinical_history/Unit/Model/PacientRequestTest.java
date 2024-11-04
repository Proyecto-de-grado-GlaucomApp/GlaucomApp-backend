package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientRequest;


public class PacientRequestTest {

    @Test
    public void testPacientRequest() {
        String name = "John Doe";
        String cedula = "123456789";
        
        PacientRequest pacientRequest = new PacientRequest(name, cedula);
        
        assertEquals(name, pacientRequest.name());
        assertEquals(cedula, pacientRequest.cedula());
    }
}