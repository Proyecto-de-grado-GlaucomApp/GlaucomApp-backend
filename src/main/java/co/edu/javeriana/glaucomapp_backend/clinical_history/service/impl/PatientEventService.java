/**
 * Service class for handling patient-related events.
 * This service listens for events related to patients and performs necessary actions.
 * 
 * Dependencies:
 * - PacientRepository: Repository for accessing patient data.
 * - ExamRepository: Repository for accessing exam data.
 * - S3Service: Service for handling S3 operations.
 * 
 * Methods:
 * - on(UUID ophtalmologistId): Listens for events with the given ophthalmologist ID, retrieves all patients
 *   associated with the doctor, deletes their exams and patients from the repositories, and deletes the 
 *   associated images from S3.
 * 
 * Annotations:
 * - @Service: Indicates that this class is a service component in the Spring context.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments (final fields).
 * - @ApplicationModuleListener: Marks the method to listen for application module events.
 * 
 * @param ophtalmologistId The ID of the ophthalmologist whose patients' data needs to be processed.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.common.S3.S3Service;
import lombok.RequiredArgsConstructor;

import org.springframework.modulith.events.ApplicationModuleListener;


@Service
@RequiredArgsConstructor
public class PatientEventService {

    private final PacientRepository pacientRepository;
    private final ExamRepository examRepository;
    private final S3Service s3Service;

    @ApplicationModuleListener
    void on(UUID ophtalmologistId){

        //initialize an empty list of url images
        List<String> urlImages = new ArrayList<>();
        //get all the patients of the doctor
        List<Pacient> patients = pacientRepository.findAllPacientsByDoctorId(ophtalmologistId);
        if (!patients.isEmpty()) {
            patients.forEach(pacient -> {
                pacient.getExams().forEach(exam -> urlImages.add(exam.getUrlImage()));
                examRepository.deleteAll(pacient.getExams());
                pacientRepository.delete(pacient);
            });
            urlImages.forEach(urlImage -> {
                s3Service.deleteImage(urlImage);
            });
        }
    }

}
