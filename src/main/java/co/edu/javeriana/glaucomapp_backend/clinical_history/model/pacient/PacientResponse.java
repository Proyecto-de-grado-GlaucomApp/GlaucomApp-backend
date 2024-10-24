/**
 * A record representing a response for a patient.
 * 
 * @param PacinetId the unique identifier of the patient
 * @param name the name of the patient
 * @param cedula the identification number of the patient
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient;

import java.util.UUID;

public record PacientResponse (UUID PacinetId, String name, String cedula){
    
}
