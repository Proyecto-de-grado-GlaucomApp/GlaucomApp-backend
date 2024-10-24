package co.edu.javeriana.glaucomapp_backend.clinical_history.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;

@Service
public interface ExamService {
    public void saveExam(String ophtalIdString, ExamRequest examRequest);

    public List<ExamsResponse> getExamsByPacient(String ophtalIdString, String pacientIdString,int startIndex, int endIndex);

    public void deleteExam(String examId);

    public ExamRes getExamById(String examId);
    
} 

