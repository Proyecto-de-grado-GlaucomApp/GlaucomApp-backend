package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ReviewOphthalmologistEvent;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl.ExamServiceImpl;
import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;

public class ExamServiceImplTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private PacientRepository pacientRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ReviewOphthalmologistEvent reviewOphthalmologistEvent;

    @InjectMocks
    private ExamServiceImpl examServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveExam() {
        String ophtalIdString = UUID.randomUUID().toString();
        ExamRequest examRequest = mock(ExamRequest.class);
        when(examRequest.cedula()).thenReturn("123456");
        when(pacientRepository.findPacientByCedulaAndDoctorId(anyString(), any(UUID.class)))
                .thenReturn(mock(Pacient.class));
        when(examRepository.save(any(Exam.class))).thenReturn(mock(Exam.class));

        examServiceImpl.saveExam(ophtalIdString, examRequest);

        verify(reviewOphthalmologistEvent).verifyOphtalmologist(ophtalIdString);
        verify(examRepository).save(any(Exam.class));
    }

    @Test
    public void testGetExamById() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        String examIdString = UUID.randomUUID().toString();
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        UUID examId = UUID.fromString(examIdString);

        Exam exam = mock(Exam.class);
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(exam.getPacient()).thenReturn(mock(Pacient.class));
        when(exam.getPacient().getId()).thenReturn(pacientId);
        when(exam.getPacient().getDoctorId()).thenReturn(ophtalId);
        when(s3Service.generatePresignedUrl(anyString())).thenReturn("url");

        ExamRes examRes = examServiceImpl.getExamById(ophtalIdString, pacientIdString, examIdString);

        assertNotNull(examRes);
        verify(examRepository).findById(examId);
    }

    @Test
    public void testGetExamById_Unauthorized() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        String examIdString = UUID.randomUUID().toString();
        UUID examId = UUID.fromString(examIdString);

        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> {
            examServiceImpl.getExamById(ophtalIdString, pacientIdString, examIdString);
        });
    }

    @Test
    public void testDeleteExam() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        String examIdString = UUID.randomUUID().toString();
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        UUID examId = UUID.fromString(examIdString);

        // Mock the Exam and its behavior
        Exam exam = mock(Exam.class);
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(exam.getPacient()).thenReturn(mock(Pacient.class));
        when(exam.getPacient().getId()).thenReturn(pacientId);
        when(exam.getPacient().getDoctorId()).thenReturn(ophtalId);

        // Mock the image URL
        String imageUrl = "http://example.com/image.png";
        when(exam.getUrlImage()).thenReturn(imageUrl); // Make sure to mock the URL retrieval

        // Call the method to delete the exam
        examServiceImpl.deleteExam(ophtalIdString, pacientIdString, examIdString);

        // Verify that the correct interactions occurred
        verify(s3Service).deleteImage(imageUrl); // Check for the correct image URL
        verify(examRepository).deleteById(examId); // Ensure the exam is deleted from the repository
    }

    @Test
    public void testDeleteExam_Unauthorized() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        String examIdString = UUID.randomUUID().toString();
        UUID examId = UUID.fromString(examIdString);

        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> {
            examServiceImpl.deleteExam(ophtalIdString, pacientIdString, examIdString);
        });
    }
}