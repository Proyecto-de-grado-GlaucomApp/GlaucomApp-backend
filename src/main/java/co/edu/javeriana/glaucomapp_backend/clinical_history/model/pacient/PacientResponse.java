package co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient;

import java.util.UUID;

public record PacientResponse (UUID PacinetId, String name, String cedula){
    
}
