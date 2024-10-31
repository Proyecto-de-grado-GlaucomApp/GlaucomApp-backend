/**
 * Service implementation for managing Pacient entities.
 * This class provides methods to create, delete, and retrieve Pacient records.
 * It also ensures that the operations are performed within the context of a transaction.
 * 
 * Dependencies:
 * 
 *   {@link PacientRepository} - Repository for Pacient entities
 *   {@link MyUserRepository} - Repository for MyUser entities
 *   {@link ExamRepository} - Repository for Exam entities
 * 
 * 
 * Methods:
 * 
 *   {@link #deletePacient(UUID)} - Deletes a Pacient by ID
 *   {@link #savePacient(PacientRequest, String)} - Saves a new Pacient
 *   {@link #getPacientsByOphtal(String, int, int)} - Retrieves a list of Pacients for a given Ophthalmologist
 *   {@link #deletePacient(String, String)} - Deletes a Pacient by Ophthalmologist ID and Pacient ID
 *   {@link #getPacientById(String, String)} - Retrieves a Pacient by Ophthalmologist ID and Pacient cedula
 * 
 * 
 * Validation Methods:
 * 
 *   {@link #validatePacientRequest(PacientRequest)} - Validates the PacientRequest object
 *   {@link #validateOphtalId(String)} - Validates the Ophthalmologist ID
 *   {@link #validatePacientId(String)} - Validates the Pacient ID
 * 
 * 
 * Exceptions:
 * 
 *   {@link IllegalArgumentException} - Thrown when input parameters are invalid
 *   {@link EntityNotFoundException} - Thrown when a Pacient or Ophthalmologist is not found
 *   {@link AccessDeniedException} - Thrown when unauthorized access is attempted
 * 
 * 
 * Transactional:
 * 
 *   Methods annotated with {@link Transactional} ensure that the operations are executed within a transaction
 * 
 * 
 * @see PacientService
 * @see PacientRepository
 * @see MyUserRepository
 * @see ExamRepository
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.PacientService;
import co.edu.javeriana.glaucomapp_backend.common.S3.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PacientServiceImpl implements PacientService {

    private final PacientRepository pacientRepository;

    
    //private final MyUserRepository myUserRepository;

    
    private final ExamRepository examRepository;
            private final S3Service s3Service;

    public PacientServiceImpl(PacientRepository pacientRepository, 
            ExamRepository examRepository,
            S3Service s3Service) {
        this.pacientRepository = pacientRepository;
        this.examRepository = examRepository;
        this.s3Service = s3Service;
    }

    /**
     * Deletes a pacient by their UUID.
     * 
     * This method performs the following actions:
     * 1. Retrieves the pacient from the repository using the provided UUID.
     * 2. Throws an EntityNotFoundException if the pacient is not found.
     * 3. Removes all exams associated with the pacient.
     * 4. Deletes the pacient from the repository.
     * 
     * @param pacientId the UUID of the pacient to be deleted
     * @throws EntityNotFoundException if the pacient is not found
     */
    @Transactional
    public void deletePacient(UUID pacientId) {
        Pacient pacient = pacientRepository.findById(pacientId)
                .orElseThrow(() -> new EntityNotFoundException("Pacient not found"));

        // Remove exams associated with the pacient
        List<Exam> exams = pacient.getExams();
        pacient.getExams().forEach(exam -> {
            s3Service.deleteImage(exam.getUrlImage());
            System.out.println("Deleting image: " + exam.getUrlImage());
        });
        
        examRepository.deleteAll(exams); // Efficiently delete all exams at once
        pacientRepository.delete(pacient);
    }

    /**
     * Saves a new pacient to the repository.
     *
     * @param pacientRequest the request object containing pacient details
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @throws IllegalArgumentException if the ophthalmologist ID is invalid or if a pacient with the same cedula already exists
     */
    @Override
    public void savePacient(PacientRequest pacientRequest, String ophtalIdString) {
        validatePacientRequest(pacientRequest);
        UUID ophtalId = UUID.fromString(ophtalIdString);


        /* Definir un evento
        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
 */

        // Check if pacient already exists
        if (pacientRepository.findPacientByCedulaAndDoctorId(pacientRequest.cedula(), ophtalId) != null) {
            throw new IllegalArgumentException("Cedula already exists");
        }

        Pacient newPacient = Pacient.builder()
                .cedula(pacientRequest.cedula())
                .name(pacientRequest.name())
                .doctorId(ophtalId)
                .build();

        pacientRepository.save(newPacient);
    }

    /**
     * Validates the given PacientRequest object.
     * <p>
     * This method checks if the PacientRequest object is null or if any of its required fields
     * (cedula or name) are null or empty. If any of these conditions are met, an 
     * IllegalArgumentException is thrown.
     * 
     * @param pacientRequest the PacientRequest object to be validated
     * @throws IllegalArgumentException if the PacientRequest object is null or if any required fields are null or empty
     */
    private void validatePacientRequest(PacientRequest pacientRequest) {
        if (pacientRequest == null) {
            throw new IllegalArgumentException("Empty Pacient is not allowed");
        }
        if (pacientRequest.cedula() == null || pacientRequest.cedula().isEmpty() ||
                pacientRequest.name() == null || pacientRequest.name().isEmpty()) {
            throw new IllegalArgumentException("Empty Pacient fields are not allowed");
        }
    }

    /**
     * Retrieves a list of patients associated with a specific ophthalmologist within a specified range.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param startIndex the starting index of the range
     * @param endIndex the ending index of the range
     * @return a list of PacientResponse objects representing the patients
     * @throws IllegalArgumentException if the ophthalmologist ID is invalid or no patients are found
     */
    @Override
    public List<PacientResponse> getPacientsByOphtal(String ophtalIdString, int startIndex, int endIndex) {
        validateOphtalId(ophtalIdString);
        UUID ophtalId = UUID.fromString(ophtalIdString);

        /*
        myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
 */

        List<Pacient> pacients = pacientRepository.findAllPacientsByDoctorId(ophtalId);
        if (pacients.isEmpty()) {
            //Return empty list if no pacients are found
            return List.of();
        }

        // Limit the range of pacients to return
        int safeEndIndex = Math.min(endIndex, pacients.size());
        List<PacientResponse> responseList = pacients.subList(startIndex, safeEndIndex).stream()
                .map(e -> new PacientResponse(e.getId(), e.getName(), e.getCedula()))
                .collect(Collectors.toList());

        return responseList;
    }

    /**
     * Validates the given ophthalmologist ID string.
     * 
     * @param ophtalIdString the ophthalmologist ID string to validate
     * @throws IllegalArgumentException if the ophthalmologist ID string is null or empty
     */
    private void validateOphtalId(String ophtalIdString) {
        if (ophtalIdString == null || ophtalIdString.isEmpty()) {
            throw new IllegalArgumentException("Empty ophtalId is not allowed");
        }
    }

    /**
     * Deletes a pacient from the repository.
     * 
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param pacientIdString the ID of the pacient as a string
     * @throws IllegalArgumentException if the ophthalmologist ID or pacient ID is invalid
     * @throws AccessDeniedException if the pacient is not associated with the given ophthalmologist
     * 
     * This method performs the following steps:
     * 1. Validates the ophthalmologist ID and pacient ID.
     * 2. Converts the ID strings to UUIDs.
     * 3. Checks if the ophthalmologist exists in the repository.
     * 4. Retrieves the pacient from the repository.
     * 5. Validates that the pacient is associated with the given ophthalmologist.
     * 6. Clears the pacient's exams.
     * 7. Deletes the pacient from the repository.
     */
    @Transactional
    @Override
    public void deletePacient(String ophtalIdString, String pacientIdString) {
        validateOphtalId(ophtalIdString);
        validatePacientId(pacientIdString);

        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);

        /*
        myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
 */

        Pacient pacient = pacientRepository.findById(pacientId)
                .orElseThrow(() -> new IllegalArgumentException("Pacient not found"));

        // Validate that the pacient is associated with the ophthalmologist
        if (!pacient.getDoctorId().equals(ophtalId)) {
            throw new AccessDeniedException("Unauthorized access to the pacient");
        }
pacient.getExams().forEach(exam -> {
    s3Service.deleteImage(exam.getUrlImage());
    System.out.println("Deleting image: " + exam.getUrlImage());
});


        // Clear exams before deleting
        pacient.getExams().clear();
        pacientRepository.delete(pacient);
    }

    /**
     * Validates the given patient ID string.
     * 
     * @param pacientIdString the patient ID string to validate
     * @throws IllegalArgumentException if the patient ID string is null or empty
     */
    private void validatePacientId(String pacientIdString) {
        if (pacientIdString == null || pacientIdString.isEmpty()) {
            throw new IllegalArgumentException("Empty Pacient Id is not allowed");
        }
    }

    /**
     * Retrieves a patient's information based on the provided ophthalmologist ID and patient's cedula.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param cedula the cedula (identification number) of the patient
     * @return a PacientResponse object containing the patient's ID, name, and cedula
     * @throws IllegalArgumentException if the ophthalmologist ID is invalid
     * @throws EntityNotFoundException if the patient is not found
     */
    @Override
    public PacientResponse getPacientById(String ophtalIdString, String cedula) {
        validateOphtalId(ophtalIdString);
        
        UUID ophtalId = UUID.fromString(ophtalIdString);

        // Definir evento
/*
        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
 */

        Pacient pacient = pacientRepository.findPacientByCedulaAndDoctorId(cedula, ophtalId);
        if (pacient == null) {
            //Return empty response if pacient is not found
            return new PacientResponse(null, null, null);
        }
        return new PacientResponse(pacient.getId(), pacient.getName(), pacient.getCedula());

    }

}
