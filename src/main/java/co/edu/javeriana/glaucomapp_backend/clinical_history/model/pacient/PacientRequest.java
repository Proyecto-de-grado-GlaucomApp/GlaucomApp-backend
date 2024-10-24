package co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient;

/**
 * A record representing a request for patient information.
 * 
 * @param name   the name of the patient
 * @param cedula the identification number of the patient
 */
public record PacientRequest (String name, String cedula){
    
}
