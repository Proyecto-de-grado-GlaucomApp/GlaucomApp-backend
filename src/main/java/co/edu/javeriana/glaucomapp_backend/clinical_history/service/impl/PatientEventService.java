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
import org.springframework.stereotype.Component;

@Service
@RequiredArgsConstructor
public class PatientEventService {

    private final PacientRepository pacientRepository;
    private final ExamRepository examRepository;
    private final S3Service s3Service;

    @ApplicationModuleListener
    void on(UUID ophtalmologistId){
                System.out.println("Doctor deleted event received");

        //initialize an empty list of url images
        List<String> urlImages = new ArrayList<>();

        // Encuentra y elimina los pacientes asociados al oftalmólogo
        List<Pacient> patients = pacientRepository.findAllPacientsByDoctorId(ophtalmologistId);
        System.out.println("Patients found: " + patients.size());
        if (!patients.isEmpty()) {
            System.out.println("Into pacients condiiton");
            patients.forEach(pacient -> {
                pacient.getExams().forEach(exam -> urlImages.add(exam.getUrlImage()));
                examRepository.deleteAll(pacient.getExams());
                pacientRepository.delete(pacient);
            });
            System.out.println("urlImages: " + urlImages);
            urlImages.forEach(urlImage -> {
                System.out.println("Deleting image: " + urlImage);
                s3Service.deleteImage(urlImage);
                //deleteImage(urlImage);
            });
        }
    }

}
