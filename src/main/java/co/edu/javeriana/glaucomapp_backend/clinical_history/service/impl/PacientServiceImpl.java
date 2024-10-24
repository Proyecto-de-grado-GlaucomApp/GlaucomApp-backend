package co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.PacientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PacientServiceImpl implements PacientService {

    @Autowired
    private PacientRepository pacientRepository;

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private ExamRepository examRepository;

    @Transactional
    public void deletePacient(UUID pacientId) {
        Pacient pacient = pacientRepository.findById(pacientId)
                .orElseThrow(() -> new EntityNotFoundException("Pacient not found"));
        // Remove exams associated with the pacient
        List<Exam> exams = pacient.getExams();
        exams.forEach(exam -> examRepository.delete(exam));
        // Now delete the pacient
        pacientRepository.delete(pacient);
    }

    @Override
    public void savePacient(PacientRequest pacient, String ophtalIdString) {
        if (pacient == null) {
            throw new IllegalArgumentException("Empty Pacient is not allowed");
        }
        if (pacient.cedula() == null || pacient.cedula().isEmpty() || pacient.name() == null
                || pacient.name().isEmpty()) {
            throw new IllegalArgumentException("Empty Pacient fields are not allowed");
        }
        if (ophtalIdString == null || ophtalIdString.isEmpty()) {
            throw new IllegalArgumentException("Empty fields are not allowed");
        }
        UUID ophtalId = UUID.fromString(ophtalIdString);
        // Fetch the MyUser (Ophthalmologist) by ID
        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));

        // Create a new Pacient using the builder pattern
        System.out.println("Pacient: " + pacient);

        Pacient newPacient = Pacient.builder()
                .cedula(pacient.cedula())
                .name(pacient.name())
                .ophthalUser(ophthalmologist).build();
        System.out.println("New Pacient: " + newPacient);
        if (pacientRepository.findPacientByCedulaAndOphthalUser(newPacient.getCedula(), ophthalmologist) != null) {
            throw new IllegalArgumentException("Pacient with cedula " + pacient.cedula() + " already exists");
        }
        pacientRepository.save(newPacient);

    }

    @Override
    public List<PacientResponse> getPacientsByOphtal(String ophtalIdString, int startIndex, int endIndex) {
        if (ophtalIdString == null || ophtalIdString.isEmpty()) {
            throw new IllegalArgumentException("Empty fields are not allowed");
        }
        UUID ophtalId = UUID.fromString(ophtalIdString);
        // Fetch the MyUser (Ophthalmologist) by ID
        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
        try {
            List<Pacient> pacients = pacientRepository.findAllPacientsByOpthalUserId(ophtalId);
            return pacients.subList(startIndex, Math.min(endIndex, pacients.size())).stream()
                    .map(p -> new PacientResponse(p.getId(), p.getName(), p.getCedula()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting pacients");
        }
    }

    @Transactional
    @Override
    public void deletePacient(String ophtalIdString, String pacientIdString) {
        if (ophtalIdString == null || ophtalIdString.isEmpty()) {
            throw new IllegalArgumentException("Empty ophtalId is not allowed");
        }
        if (pacientIdString == null || pacientIdString.isEmpty()) {
            throw new IllegalArgumentException("Empty Pacient Id is not allowed");
        }
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        // Fetch the MyUser (Ophthalmologist) by ID
        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));

        // Fetch the Pacient by ID
        Pacient pacient = pacientRepository.findById(pacientId)
                .orElseThrow(() -> new IllegalArgumentException("Pacient not found"));
        pacient.getExams().clear(); // Esto desvincula los exámenes del paciente.
        try {
            pacientRepository.delete(pacient);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting pacient: " + e.getMessage());
        }
        // TODO: Pacient doesnt exist
    }

}
