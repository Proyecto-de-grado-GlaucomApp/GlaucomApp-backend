package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;
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
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;
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

    @Test
    public void testGetExamsByPacient_Success() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        int startIndex = 0;
        int endIndex = 2;

        List<Exam> exams = new ArrayList<>();
        exams.add(new Exam());
        exams.add(new Exam());

        when(examRepository.findByPacient_DoctorIdAndPacient_Id(any(UUID.class), any(UUID.class))).thenReturn(exams);

        List<ExamsResponse> result = examServiceImpl.getExamsByPacient(ophtalIdString, pacientIdString, startIndex, endIndex);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reviewOphthalmologistEvent, times(1)).verifyOphtalmologist(ophtalIdString);
        verify(examRepository, times(1)).findByPacient_DoctorIdAndPacient_Id(any(UUID.class), any(UUID.class));
    }

    @Test
    public void testGetExamsByPacient_EmptyList() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        int startIndex = 0;
        int endIndex = 2;

        when(examRepository.findByPacient_DoctorIdAndPacient_Id(any(UUID.class), any(UUID.class))).thenReturn(new ArrayList<>());

        List<ExamsResponse> result = examServiceImpl.getExamsByPacient(ophtalIdString, pacientIdString, startIndex, endIndex);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewOphthalmologistEvent, times(1)).verifyOphtalmologist(ophtalIdString);
        verify(examRepository, times(1)).findByPacient_DoctorIdAndPacient_Id(any(UUID.class), any(UUID.class));
    }

    @Test
    public void testGetExamsByPacient_InvalidUUID() {
        String invalidOphtalIdString = "invalid-uuid";
        String pacientIdString = UUID.randomUUID().toString();
        int startIndex = 0;
        int endIndex = 2;

        assertThrows(IllegalArgumentException.class, () -> {
            examServiceImpl.getExamsByPacient(invalidOphtalIdString, pacientIdString, startIndex, endIndex);
        });

        verify(reviewOphthalmologistEvent, never()).verifyOphtalmologist(anyString());
        verify(examRepository, never()).findByPacient_DoctorIdAndPacient_Id(any(UUID.class), any(UUID.class));
    }

    @Test
    public void testGetExamsByPacient_PartialList() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        int startIndex = 1;
        int endIndex = 3;

        List<Exam> exams = new ArrayList<>();
        exams.add(new Exam());
        exams.add(new Exam());
        exams.add(new Exam());

        when(examRepository.findByPacient_DoctorIdAndPacient_Id(any(UUID.class), any(UUID.class))).thenReturn(exams);

        List<ExamsResponse> result = examServiceImpl.getExamsByPacient(ophtalIdString, pacientIdString, startIndex, endIndex);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reviewOphthalmologistEvent, times(1)).verifyOphtalmologist(ophtalIdString);
        verify(examRepository, times(1)).findByPacient_DoctorIdAndPacient_Id(any(UUID.class), any(UUID.class));
    }

    @Test
    public void testVerifyExam_Success() throws Exception {
        UUID ophtalId = UUID.randomUUID();
        UUID pacientId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();

        Pacient pacient = new Pacient();
        pacient.setId(pacientId);
        pacient.setDoctorId(ophtalId);

        Exam exam = new Exam();
        exam.setPacient(pacient);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        Method method = ExamServiceImpl.class.getDeclaredMethod("verifyExam", UUID.class, UUID.class, UUID.class);
        method.setAccessible(true);
        Exam result = (Exam) method.invoke(examServiceImpl, ophtalId, pacientId, examId);

        assertNotNull(result);
        assertEquals(exam, result);
    }

        @Test
    public void testVerifyExam_ExamNotFound() throws Exception {
        UUID ophtalId = UUID.randomUUID();
        UUID pacientId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();

        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        Method method = ExamServiceImpl.class.getDeclaredMethod("verifyExam", UUID.class, UUID.class, UUID.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> method.invoke(examServiceImpl, ophtalId, pacientId, examId));
        assertTrue(exception.getCause() instanceof AccessDeniedException);
    }

    @Test
    public void testVerifyExam_UnauthorizedAccess() throws Exception {
        UUID ophtalId = UUID.randomUUID();
        UUID pacientId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();

        Pacient pacient = new Pacient();
        pacient.setId(UUID.randomUUID()); // Different pacient ID
        pacient.setDoctorId(ophtalId);

        Exam exam = new Exam();
        exam.setPacient(pacient);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        Method method = ExamServiceImpl.class.getDeclaredMethod("verifyExam", UUID.class, UUID.class, UUID.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> method.invoke(examServiceImpl, ophtalId, pacientId, examId));
        assertTrue(exception.getCause() instanceof AccessDeniedException);
    }

    @Test
    public void testVerifyExam_UnauthorizedDoctorAccess() throws Exception {
        UUID ophtalId = UUID.randomUUID();
        UUID pacientId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();

        Pacient pacient = new Pacient();
        pacient.setId(pacientId);
        pacient.setDoctorId(UUID.randomUUID()); // Different doctor ID

        Exam exam = new Exam();
        exam.setPacient(pacient);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        Method method = ExamServiceImpl.class.getDeclaredMethod("verifyExam", UUID.class, UUID.class, UUID.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> method.invoke(examServiceImpl, ophtalId, pacientId, examId));
        assertTrue(exception.getCause() instanceof AccessDeniedException);
    }

    @Test
    public void testVerifyExam_PacientIsNull() throws Exception {
        UUID ophtalId = UUID.randomUUID();
        UUID pacientId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();

        Exam exam = new Exam();
        exam.setPacient(null); // Pacient is null

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        Method method = ExamServiceImpl.class.getDeclaredMethod("verifyExam", UUID.class, UUID.class, UUID.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> method.invoke(examServiceImpl, ophtalId, pacientId, examId));
        assertTrue(exception.getCause() instanceof AccessDeniedException);
    }
}