/**
 * Service implementation for managing Pacient entities.
 * This service provides methods to create, retrieve, and delete Pacient records.
 * It also handles the validation of input data and ensures that operations are authorized.
 * 
 * Dependencies:
 * - PacientRepository: Repository for Pacient entities.
 * - ExamRepository: Repository for Exam entities.
 * - S3Service: Service for managing images stored in S3.
 * - ReviewOphthalmologistEvent: Service for verifying ophthalmologist credentials.
 * 
 * Methods:
 * - deletePacient(UUID pacientId): Deletes a Pacient and its associated Exams by Pacient ID.
 * - savePacient(PacientRequest pacientRequest, String ophtalIdString): Saves a new Pacient.
 * - getPacientsByOphtal(String ophtalIdString, int startIndex, int endIndex): Retrieves a list of Pacients for a given ophthalmologist.
 * - deletePacient(String ophtalIdString, String pacientIdString): Deletes a Pacient by Pacient ID and Ophthalmologist ID.
 * - getPacientById(String ophtalIdString, String cedula): Retrieves a Pacient by Cedula and Ophthalmologist ID.
 * 
 * Private Methods:
 * - validatePacientRequest(PacientRequest pacientRequest): Validates the PacientRequest object.
 * - validateOphtalId(String ophtalIdString): Validates the ophthalmologist ID.
 * - validatePacientId(String pacientIdString): Validates the Pacient ID.
 * 
 * Annotations:
 * - @Service: Indicates that this class is a service component in the Spring context.
 * - @Transactional: Ensures that methods are executed within a transaction context.
 * 
 * Exceptions:
 * - EntityNotFoundException: Thrown when a Pacient is not found.
 * - IllegalArgumentException: Thrown when input data is invalid.
 * - AccessDeniedException: Thrown when an unauthorized access attempt is made.
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
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.PatientService;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ReviewOphthalmologistEvent;
import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PacientServiceImpl implements PatientService {

    private final PacientRepository pacientRepository;
    
    private final ReviewOphthalmologistEvent reviewOphthalmologistEvent;

    private final ExamRepository examRepository;

    private final S3Service s3Service;

    public PacientServiceImpl(PacientRepository pacientRepository, 
            ExamRepository examRepository,
            S3Service s3Service, ReviewOphthalmologistEvent reviewOphthalmologistEvent) {
        this.pacientRepository = pacientRepository;
        this.examRepository = examRepository;
        this.s3Service = s3Service;
        this.reviewOphthalmologistEvent = reviewOphthalmologistEvent;
    }

    /**
     * Deletes a patient and all associated exams from the database.
     * 
     * @param pacientId the UUID of the patient to be deleted
     * @throws EntityNotFoundException if the patient with the given ID is not found
     */
    @Transactional
    public void deletePacient(UUID pacientId) {
        Pacient pacient = pacientRepository.findById(pacientId)
                .orElseThrow(() -> new EntityNotFoundException("Pacient not found"));

        List<Exam> exams = pacient.getExams();
        exams.forEach(exam -> {
            s3Service.deleteImage(exam.getUrlImage());
        });

        examRepository.deleteAll(exams);
        pacientRepository.delete(pacient);
    }

    /**
     * Saves a new patient record in the repository.
     *
     * @param pacientRequest the request object containing patient details
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @throws IllegalArgumentException if the patient's cedula already exists
     */
    @Override
    public void savePacient(PacientRequest pacientRequest, String ophtalIdString) {
        validatePacientRequest(pacientRequest);
        UUID ophtalId = UUID.fromString(ophtalIdString);

        reviewOphthalmologistEvent.verifyOphtalmologist(ophtalIdString);

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
     * 
     * @param pacientRequest the PacientRequest object to validate
     * @throws IllegalArgumentException if the pacientRequest is null, 
     *         or if the cedula or name fields are null or empty
     */
    private void validatePacientRequest(PacientRequest pacientRequest) {
        if (pacientRequest == null || 
            pacientRequest.cedula() == null || pacientRequest.cedula().isEmpty() ||
            pacientRequest.name() == null || pacientRequest.name().isEmpty()) {
            throw new IllegalArgumentException("Pacient request and its fields must not be empty");
        }
    }


    /**
     * Retrieves a list of patients associated with a specific ophthalmologist.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param startIndex the starting index of the sublist to be returned
     * @param endIndex the ending index of the sublist to be returned
     * @return a list of PacientResponse objects representing the patients
     *         associated with the specified ophthalmologist within the given range
     * @throws IllegalArgumentException if the ophthalmologist ID is invalid
     */
    @Override
    public List<PacientResponse> getPacientsByOphtal(String ophtalIdString, int startIndex, int endIndex) {
        validateOphtalId(ophtalIdString);
        UUID ophtalId = UUID.fromString(ophtalIdString);

        reviewOphthalmologistEvent.verifyOphtalmologist(ophtalIdString);

        List<Pacient> pacients = pacientRepository.findAllPacientsByDoctorId(ophtalId);
        if (pacients.isEmpty()) {
            return List.of(); // Return empty list if no pacients are found
        }

        int safeEndIndex = Math.min(endIndex, pacients.size());
        return pacients.subList(startIndex, safeEndIndex).stream()
                .map(p -> new PacientResponse(p.getId(), p.getName(), p.getCedula()))
                .collect(Collectors.toList());
    }


    /**
     * Validates the given ophthalmologist ID string.
     * 
     * @param ophtalIdString the ophthalmologist ID string to validate
     * @throws IllegalArgumentException if the ophthalmologist ID is null or empty
     */
    private void validateOphtalId(String ophtalIdString) {
        if (ophtalIdString == null || ophtalIdString.isEmpty()) {
            throw new IllegalArgumentException("Ophthalmologist ID must not be empty");
        }
    }


    /**
     * Deletes a pacient from the repository.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param pacientIdString the ID of the pacient as a string
     * @throws IllegalArgumentException if the pacient is not found
     * @throws AccessDeniedException if the ophthalmologist is not authorized to access the pacient
     */
    @Transactional
    @Override
    public void deletePacient(String ophtalIdString, String pacientIdString) {
        validateOphtalId(ophtalIdString);
        validatePacientId(pacientIdString);

        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);

        reviewOphthalmologistEvent.verifyOphtalmologist(ophtalIdString);

        Pacient pacient = pacientRepository.findById(pacientId)
                .orElseThrow(() -> new IllegalArgumentException("Pacient not found"));

        if (!pacient.getDoctorId().equals(ophtalId)) {
            throw new AccessDeniedException("Unauthorized access to the pacient");
        }

        pacient.getExams().forEach(exam -> {
            s3Service.deleteImage(exam.getUrlImage());
        });

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
            throw new IllegalArgumentException("Pacient ID must not be empty");
        }
    }


    /**
     * Retrieves a patient's information based on the provided ophthalmologist ID and patient's cedula.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param cedula the cedula (identification number) of the patient
     * @return a PacientResponse containing the patient's ID, name, and cedula, or an empty response if the patient is not found
     * @throws IllegalArgumentException if the ophthalmologist ID is invalid
     */
    @Override
    public PacientResponse getPacientById(String ophtalIdString, String cedula) {
        validateOphtalId(ophtalIdString);

        UUID ophtalId = UUID.fromString(ophtalIdString);
        reviewOphthalmologistEvent.verifyOphtalmologist(ophtalIdString);

        Pacient pacient = pacientRepository.findPacientByCedulaAndDoctorId(cedula, ophtalId);
        if (pacient == null) {
            return new PacientResponse(null, null, null); // Return an empty response if not found
        }

        return new PacientResponse(pacient.getId(), pacient.getName(), pacient.getCedula());
    }

}
