/**
 * Service implementation for managing exams.
 * This class provides methods to save, retrieve, and delete exams for patients.
 * It ensures that the operations are performed by authorized ophthalmologists.
 * 
 * Dependencies:
 *
 *   {@link ExamRepository}
 *   {@link MyUserRepository}
 *   {@link PacientRepository}
 * 
 * 
 * Methods:
 * 
 *   {@link #saveExam(String, ExamRequest)} - Saves a new exam for a patient.
 *   {@link #getExamsByPacient(String, String, int, int)} - Retrieves a list of exams for a patient within a specified range.
 *   {@link #deleteExam(String, String, String)} - Deletes an exam for a patient.
 *   {@link #getExamById(String, String, String)} - Retrieves an exam by its ID.
 *   {@link #verifyExam(UUID, UUID, UUID)} - Verifies the relationship between an exam, patient, and ophthalmologist.
 * 
 * 
 * Annotations:
 * 
 *   {@link Service} - Indicates that this class is a service component in the Spring framework.
 *   {@link Transactional} - Ensures that the delete operation is performed within a transaction.
 * 
 * 
 * Exceptions:
 * 
 *   {@link IllegalArgumentException} - Thrown when invalid IDs are provided or when no exams are found.
 *   {@link AccessDeniedException} - Thrown when unauthorized access to an exam is attempted.
 * 
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ExamService;
import jakarta.transaction.Transactional;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private MyUserRepository myUserRepository;

    @Autowired
    private PacientRepository pacientRepository;

    /**
     * Saves an exam for a given ophthalmologist and patient.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param examRequest the request object containing exam details
     * @throws IllegalArgumentException if the ophthalmologist ID is invalid or the patient is not found
     */
    @Override
    public void saveExam(String ophtalIdString, ExamRequest examRequest) {
        UUID ophtalId = UUID.fromString(ophtalIdString);

        // Verify that the ophtalmologist is correct
        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));

        // Find ophtal pacient
        Pacient pacient = pacientRepository.findPacientByCedulaAndOphthalUser(examRequest.cedula(), ophthalmologist);
        if (pacient == null) {
            throw new IllegalArgumentException("Patient not found for the given cedula");
        }

        // Create new exam
        Exam newExam = Exam.builder()
                .name(examRequest.name())
                .date(examRequest.date())
                .urlImage(examRequest.urlImage())
                .distanceRatio(examRequest.distanceRatio())
                .perimeterRatio(examRequest.perimeterRatio())
                .areaRatio(examRequest.areaRatio())
                .pacient(pacient)
                .build();

        // Save exam
        examRepository.save(newExam);
    }

    /**
     * Retrieves a list of exams for a specific patient within a specified range.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param pacientIdString the ID of the patient as a string
     * @param startIndex the starting index of the exams to return
     * @param endIndex the ending index of the exams to return
     * @return a list of ExamsResponse objects containing exam details
     * @throws IllegalArgumentException if the ophthalmologist ID is invalid or no exams are found for the specified patient
     */
    @Override
    public List<ExamsResponse> getExamsByPacient(String ophtalIdString, String pacientIdString, int startIndex,
            int endIndex) {
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);

        // Verify if the ophtal is valid
        myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
        // Obtain pacient exams
        List<Exam> exams = examRepository.findByPacient_OphthalUser_IdAndPacient_Id(ophtalId, pacientId);
        if (exams.isEmpty()) {
           //Return empty list if no exams are found
            return List.of();
        }

        // Limit the range of exams to return
        int safeEndIndex = Math.min(endIndex, exams.size());
        List<ExamsResponse> responseList = exams.subList(startIndex, safeEndIndex).stream()
                .map(e -> new ExamsResponse(e.getId(), e.getName(), e.getDate(), e.getUrlImage()))
                .collect(Collectors.toList());
        return responseList;
    }

    /**
     * Deletes an exam based on the provided ophthalmologist ID, patient ID, and exam ID.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param pacientIdString the ID of the patient as a string
     * @param examIdString the ID of the exam as a string
     * @throws IllegalArgumentException if any of the provided IDs are invalid
     * @throws EntityNotFoundException if the exam, patient, or ophthalmologist does not exist
     */
    @Transactional
    @Override
    public void deleteExam(String ophtalIdString, String pacientIdString, String examIdString) {
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        UUID examId = UUID.fromString(examIdString);

        // Verify the relaction between exam, pacient and ophtal are correct
        verifyExam(ophtalId, pacientId, examId);

        // Delete exam
        examRepository.deleteById(examId);
    }

    /**
     * Retrieves an exam by its ID, verifying the relationship between the exam, patient, and ophthalmologist.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param pacientIdString the ID of the patient as a string
     * @param examIdString the ID of the exam as a string
     * @return an ExamRes object containing the details of the exam
     * @throws IllegalArgumentException if the exam is not found or if the relationship verification fails
     */
    @Override
    public ExamRes getExamById(String ophtalIdString, String pacientIdString, String examIdString) {
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        UUID examId = UUID.fromString(examIdString);

        // Verify the relaction between exam, pacient and ophtal are correct
        verifyExam(ophtalId, pacientId, examId);

        // find the exam
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        // Create the exam on the Response format
        return new ExamRes(
                exam.getId(),
                exam.getName(),
                exam.getDate(),
                exam.getUrlImage(),
                exam.getDistanceRatio(),
                exam.getPerimeterRatio(),
                exam.getAreaRatio());
    }

    /**
     * Verifies the existence of an exam and checks if the exam is associated with the given patient and 
     * if the patient is associated with the given ophthalmologist.
     *
     * @param ophtalId the UUID of the ophthalmologist
     * @param pacientId the UUID of the patient
     * @param examId the UUID of the exam
     * @throws IllegalArgumentException if the exam is not found
     * @throws AccessDeniedException if the exam is not associated with the given patient or 
     *         if the patient is not associated with the given ophthalmologist
     */
    public void verifyExam(UUID ophtalId, UUID pacientId, UUID examId) {
        // Verify that the exam exist
        Optional<Exam> examOptional = examRepository.findById(examId);
        if (examOptional.isEmpty()) {
            throw new IllegalArgumentException("Exam not found");
        }
        Exam exam = examOptional.get();

        // Verify that the exam is associate to the pacient and the pacient is associate
        // to the ophtal
        Pacient pacient = exam.getPacient();

        if (pacient == null || !pacient.getId().equals(pacientId)
                || !pacient.getOphthalUser().getId().equals(ophtalId)) {
            throw new AccessDeniedException("Unauthorized access to the exam");
        }

    }

}