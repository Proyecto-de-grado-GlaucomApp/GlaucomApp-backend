package co.edu.javeriana.glaucomapp_backend.clinical_history.service;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.event.OphtalmologistDeletedEvent;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.event.EventListener;
import java.util.UUID;

@Service
public class DoctorEventListener {
    
    private final PacientRepository pacientRepository;
    private final ExamRepository examRepository;

    public DoctorEventListener(PacientRepository pacientRepository, ExamRepository examRepository) {
        this.pacientRepository = pacientRepository;
        this.examRepository = examRepository;
    }

    @EventListener
    public void handleOphtalmologistDeletedEvent(OphtalmologistDeletedEvent event) {
        System.out.println("Doctor deleted event received");
        UUID  ophtalmologistId = event.getOphtalmologistId();
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
        }
    }
}

