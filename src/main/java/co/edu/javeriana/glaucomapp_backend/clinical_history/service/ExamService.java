/**
 * Service interface for managing exams in the clinical history.
 * Provides methods to save, retrieve, and delete exams.
 * 
 * @service ExamService
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;

@Service
public interface ExamService {
    public void saveExam(String ophtalIdString, ExamRequest examRequest);

    public List<ExamsResponse> getExamsByPacient(String ophtalIdString, String pacientIdString,int startIndex, int endIndex);

    public void deleteExam(String ophtalIdString, String pacientIdString,String examId);

    public ExamRes getExamById(String ophtalIdString, String pacientIdString,String examId);
    
} 

