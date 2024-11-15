/**
 * Service interface for managing patients in the GlaucomApp application.
 * Provides methods for saving, retrieving, and deleting patient records.
 * 
 * @author 
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.service;


import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;

@Service
public interface PatientService {
    public void savePacient(PacientRequest pacient, String ophtalId);

    public List<PacientResponse> getPacientsByOphtal(String ophtalIdString, int startIndex, int endIndex);

    public void deletePacient(String ophtalIdString, String pacientId);

    public PacientResponse getPacientById(String ophtalIdString, String cedula);
}
