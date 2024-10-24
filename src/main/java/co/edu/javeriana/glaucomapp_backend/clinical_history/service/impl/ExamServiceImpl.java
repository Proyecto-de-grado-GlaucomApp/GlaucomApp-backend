package co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.id.uuid.UuidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;
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

    @Override
    public void saveExam(String ophtalIdString, ExamRequest examRequest) {
        UUID ophtalId = UUID.fromString(ophtalIdString);

        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
        Pacient pacient = pacientRepository.findPacientByCedulaAndOphthalUser(examRequest.cedula(), ophthalmologist);

        Exam newExam = Exam.builder()
                .name(examRequest.name())
                .date(examRequest.date())
                .urlImage(examRequest.urlImage())
                .distanceRatio(examRequest.distanceRatio())
                .perimeterRatio(examRequest.perimeterRatio())
                .areaRatio(examRequest.areaRatio())
                .pacient(pacient)
                .build();

        examRepository.save(newExam);

    }

    @Override
    public List<ExamsResponse> getExamsByPacient(String ophtalIdString, String pacientIdString, int startIndex,
            int endIndex) {
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        MyUser ophthalmologist = myUserRepository.findById(ophtalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
        try {
            List<Exam> exams = examRepository.findByPacient_OphthalUser_IdAndPacient_Id(ophtalId, pacientId);
            return exams.subList(startIndex, Math.min(endIndex, exams.size())).stream()
                    .map(e -> new ExamsResponse(e.getId(), e.getName(), e.getDate(), e.getUrlImage()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting exams");
        }
    }

    @Transactional
    @Override
    public void deleteExam(String examIdString) {

        UUID examId = UUID.fromString(examIdString);
        try {
            examRepository.deleteById(examId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting exam");
        }
    }

    @Override
    public ExamRes getExamById(String examIdString) {
        UUID examId = UUID.fromString(examIdString);
        try {
            Optional<Exam> examOptional = examRepository.findById(examId);
            Exam exam = examOptional.orElseThrow(() -> new IllegalArgumentException("Exam not found"));

            // Crea y devuelve el ExamResponse
            return new ExamRes(
                    exam.getId(),
                    exam.getName(),
                    exam.getDate(),
                    exam.getUrlImage(),
                    exam.getDistanceRatio(),
                    exam.getPerimeterRatio(),
                    exam.getAreaRatio());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving exam", e);
        }
    }

}
