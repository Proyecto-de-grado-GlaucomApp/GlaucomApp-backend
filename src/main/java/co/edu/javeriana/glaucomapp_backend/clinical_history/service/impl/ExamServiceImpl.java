/**
 * Service implementation for managing exams.
 * This service provides methods to save, retrieve, and delete exams for patients.
 * It also verifies the ophthalmologist's access to the exams.
 * 
 * Dependencies:
 * - ExamRepository: Repository for accessing exam data.
 * - S3Service: Service for handling S3 operations.
 * - ReviewOphthalmologistEvent: Event for verifying ophthalmologist access.
 * - PacientRepository: Repository for accessing patient data.
 * 
 * Methods:
 * - saveExam(String ophtalIdString, ExamRequest examRequest): Saves a new exam for a patient.
 * - getExamsByPacient(String ophtalIdString, String pacientIdString, int startIndex, int endIndex): Retrieves a list of exams for a patient.
 * - deleteExam(String ophtalIdString, String pacientIdString, String examIdString): Deletes an exam for a patient.
 * - getExamById(String ophtalIdString, String pacientIdString, String examIdString): Retrieves an exam by its ID.
 * 
 * Private Methods:
 * - findPacientByCedulaAndDoctorId(String cedula, UUID ophtalId): Finds a patient by their cedula and doctor ID.
 * - createExamFromRequest(ExamRequest examRequest, Pacient pacient): Creates an exam entity from an exam request.
 * - mapToExamRes(Exam exam): Maps an exam entity to an ExamRes response.
 * - mapToExamsResponse(Exam exam): Maps an exam entity to an ExamsResponse response.
 * - verifyExam(UUID ophtalId, UUID pacientId, UUID examId): Verifies the access to an exam.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ExamService;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ReviewOphthalmologistEvent;
import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;
import jakarta.transaction.Transactional;

@Service
public class ExamServiceImpl implements ExamService {

    
    private final ExamRepository examRepository;
    private final S3Service s3Service;
    private final ReviewOphthalmologistEvent reviewOphthalmologistEvent;

    private final PacientRepository pacientRepository;

    public ExamServiceImpl(ExamRepository examRepository,
            PacientRepository pacientRepository, S3Service s3Service, ReviewOphthalmologistEvent reviewOphthalmologistEvent) {
        this.examRepository = examRepository;
        this.pacientRepository = pacientRepository;
        this.s3Service = s3Service;
        this.reviewOphthalmologistEvent = reviewOphthalmologistEvent;
    }

    /**
     * Saves an exam for a given ophthalmologist and patient.
     *
     * @param ophtalIdString the UUID string of the ophthalmologist
     * @param examRequest the request object containing exam details
     * @throws IllegalArgumentException if the UUID string is invalid
     */
    @Override
    public void saveExam(String ophtalIdString, ExamRequest examRequest) {
        UUID ophtalId = UUID.fromString(ophtalIdString);

        reviewOphthalmologistEvent.verifyOphtalmologist(ophtalIdString);

        Pacient pacient = findPacientByCedulaAndDoctorId(examRequest.cedula(), ophtalId);

        Exam newExam = createExamFromRequest(examRequest, pacient);
        examRepository.save(newExam);
    }

    /**
     * Retrieves a list of exams for a specific patient within a specified range.
     *
     * @param ophtalIdString the UUID string of the ophthalmologist
     * @param pacientIdString the UUID string of the patient
     * @param startIndex the starting index of the exams to retrieve
     * @param endIndex the ending index of the exams to retrieve
     * @return a list of ExamsResponse objects representing the exams for the patient
     * @throws IllegalArgumentException if the UUID strings are invalid
     */
    public List<ExamsResponse> getExamsByPacient(String ophtalIdString, String pacientIdString, int startIndex, int endIndex) {
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        reviewOphthalmologistEvent.verifyOphtalmologist(ophtalIdString);

        List<Exam> exams = examRepository.findByPacient_DoctorIdAndPacient_Id(ophtalId, pacientId);
        if (exams.isEmpty()) {
            return List.of();
        }

        return exams.stream()
                    .skip(startIndex)
                    .limit(Math.max(0, endIndex - startIndex))
                    .map(this::mapToExamsResponse)
                    .collect(Collectors.toList());
    }

    /**
     * Deletes an exam record from the database and removes the associated image from S3 storage.
     *
     * @param ophtalIdString the UUID string of the ophthalmologist
     * @param pacientIdString the UUID string of the patient
     * @param examIdString the UUID string of the exam
     * @throws IllegalArgumentException if any of the UUID strings are invalid
     * @throws EntityNotFoundException if the exam does not exist
     */
    @Transactional
    @Override
    public void deleteExam(String ophtalIdString, String pacientIdString, String examIdString) {
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        UUID examId = UUID.fromString(examIdString);

        Exam exam = verifyExam(ophtalId, pacientId, examId);
        s3Service.deleteImage(exam.getUrlImage());

        examRepository.deleteById(examId);

    }

    /**
     * Retrieves an exam by its ID, along with the associated ophthalmologist and patient IDs.
     *
     * @param ophtalIdString the ID of the ophthalmologist as a string
     * @param pacientIdString the ID of the patient as a string
     * @param examIdString the ID of the exam as a string
     * @return an ExamRes object containing the details of the exam
     * @throws IllegalArgumentException if any of the provided IDs are invalid
     */
    @Override
    public ExamRes getExamById(String ophtalIdString, String pacientIdString, String examIdString) {
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        UUID examId = UUID.fromString(examIdString);

        Exam exam = verifyExam(ophtalId, pacientId, examId);

        return mapToExamRes(exam);
    }

    /**
     * Finds a patient by their cedula and the doctor's ID.
     *
     * @param cedula the cedula (identification number) of the patient
     * @param ophtalId the UUID of the doctor
     * @return the patient found
     * @throws IllegalArgumentException if no patient is found for the given cedula and doctor ID
     */
    private Pacient findPacientByCedulaAndDoctorId(String cedula, UUID ophtalId) {
        return Optional.ofNullable(pacientRepository.findPacientByCedulaAndDoctorId(cedula, ophtalId))
                       .orElseThrow(() -> new IllegalArgumentException("Patient not found for the given cedula"));
    }

    /**
     * Creates an Exam object from the given ExamRequest and Pacient.
     *
     * @param examRequest the request object containing exam details
     * @param pacient the Pacient object associated with the exam
     * @return a new Exam object populated with data from the examRequest and pacient
     */
    private Exam createExamFromRequest(ExamRequest examRequest, Pacient pacient) {
        return Exam.builder()
                   .name(examRequest.name())
                   .date(examRequest.date())
                   .urlImage(examRequest.urlImage())
                   .distanceRatio(examRequest.distanceRatio())
                   .perimeterRatio(examRequest.perimeterRatio())
                   .areaRatio(examRequest.areaRatio())
                   .neuroretinalRimPerimeter(examRequest.neuroretinalRimPerimeter())
                   .neuroretinalRimArea(examRequest.neuroretinalRimArea())
                   .excavationPerimeter(examRequest.excavationPerimeter())
                   .excavationArea(examRequest.excavationArea())
                   .state(examRequest.state())
                   .ddlStage(examRequest.ddlStage())
                   .pacient(pacient)
                   .build();
    }

    /**
     * Maps an Exam entity to an ExamRes response object.
     *
     * @param exam the Exam entity to be mapped
     * @return an ExamRes object containing the mapped data
     */
    private ExamRes mapToExamRes(Exam exam) {
        return new ExamRes(
                exam.getId(),
                exam.getName(),
                exam.getDate(),
                s3Service.generatePresignedUrl(exam.getUrlImage()),
                exam.getDistanceRatio(),
                exam.getPerimeterRatio(),
                exam.getAreaRatio(),
                exam.getNeuroretinalRimPerimeter(),
                exam.getNeuroretinalRimArea(),
                exam.getExcavationPerimeter(),
                exam.getExcavationArea(),
                exam.getState(),
                exam.getDdlStage()
        );
    }

    /**
     * Maps an Exam object to an ExamsResponse object.
     *
     * @param exam the Exam object to be mapped
     * @return an ExamsResponse object containing the mapped data from the Exam object
     */
    private ExamsResponse mapToExamsResponse(Exam exam) {
        return new ExamsResponse(
                exam.getId(),
                exam.getName(),
                exam.getDate(),
                s3Service.generatePresignedUrl(exam.getUrlImage())
        );
    }

    /**
     * Verifies the existence and ownership of an exam by checking if the exam exists and if it belongs to the specified patient and ophthalmologist.
     *
     * @param ophtalId the UUID of the ophthalmologist
     * @param pacientId the UUID of the patient
     * @param examId the UUID of the exam to be verified
     * @return the verified Exam object if it exists and belongs to the specified patient and ophthalmologist
     * @throws AccessDeniedException if the exam does not exist or does not belong to the specified patient and ophthalmologist
     */
    private Exam verifyExam(UUID ophtalId, UUID pacientId, UUID examId) {
        return examRepository.findById(examId)
                             .filter(exam -> exam.getPacient() != null
                                     && exam.getPacient().getId().equals(pacientId)
                                     && exam.getPacient().getDoctorId().equals(ophtalId))
                             .orElseThrow(() -> new AccessDeniedException("Unauthorized access to the exam"));
    }

}