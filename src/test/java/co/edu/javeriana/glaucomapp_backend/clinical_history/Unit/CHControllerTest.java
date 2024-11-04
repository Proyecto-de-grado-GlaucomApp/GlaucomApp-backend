package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import co.edu.javeriana.glaucomapp_backend.clinical_history.controller.CHController;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.ErrorResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ExamService;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.PatientService;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CHControllerTest {
    @Mock
    private PatientService pacientService;

    @Mock
    private ExamService examService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private CHController chController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    
    @Test
    public void testSavePacient_Success() throws Exception {
        // Arrange
        PacientRequest pacient = new PacientRequest("", "");
        String token = "Bearer valid_token";
        String ophtalId = "ophtal_id";

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);

        // Act
        ResponseEntity<?> response = chController.savePacient(token, pacient);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Pacient saved successfully", response.getBody());
    }

    
    @Test
    public void testSavePacient_UsernameAlreadyInUse() throws Exception {
        // Arrange
        PacientRequest pacient = new PacientRequest("", "");
        String token = "Bearer valid_token";

        when(jwtUtil.extractIdFromToken(token)).thenReturn("ophtal_id");
        doThrow(new IllegalArgumentException("Username already in use")).when(pacientService).savePacient(pacient,
                "ophtal_id");

        // Act
        ResponseEntity<?> response;
        try {
            response = chController.savePacient(token, pacient);
        } catch (IllegalArgumentException e) {
            response = ResponseEntity.status(400).body(new ErrorResponse("Conflict", e.getMessage()));
        }

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Username already in use", ((ErrorResponse) response.getBody()).getMessage());
    }

    
    @Test
    public void testGetPacient_Success() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String cedula = "123456";
        String ophtalId = "ophtal_id";

        PacientResponse pacientResponse = new PacientResponse(UUID.randomUUID(), "", "");
        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);
        when(pacientService.getPacientById(ophtalId, cedula)).thenReturn(pacientResponse);

        // Act
        ResponseEntity<?> response = chController.getPacient(token, cedula);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(pacientResponse, response.getBody());
    }

    
    @Test
    public void testGetPacient_NotFound() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String cedula = "123456";
        String ophtalId = "ophtal_id";

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);
        when(pacientService.getPacientById(ophtalId, cedula)).thenReturn(null);

        // Act
        ResponseEntity<?> response = chController.getPacient(token, cedula);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Pacient not found", response.getBody());
    }

    
    @Test
    public void testGetPacientsByOphtal_Success() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String ophtalId = "ophtal_id";
        List<PacientResponse> pacientList = Collections.singletonList(new PacientResponse(UUID.randomUUID(), "", ""));

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);
        when(pacientService.getPacientsByOphtal(ophtalId, 0, 10)).thenReturn(pacientList);

        // Act
        ResponseEntity<?> response = chController.getPacientsByOphtal(token, 0, 10);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(pacientList, response.getBody());
    }

    
    @Test
    public void testDeletePacient_Success() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String pacientId = "123456";
        String ophtalId = "ophtal_id";

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);

        // Act
        ResponseEntity<?> response = chController.deletePacient(token, pacientId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Pacient deleted successfully", response.getBody());
    }

    
    @Test
    public void testSaveExam_Success() throws Exception {
        // Arrange
        ExamRequest examRequest = new ExamRequest(
                "123456", // String cedula
                "Exam Name", // String name
                "2023-10-31", // String date
                "http://example.com/image.png", // String urlImage
                0.0, // Double distanceRatio
                0.0, // Double perimeterRatio
                0.0, // Double areaRatio
                0.0, // Double neuroretinalRimPerimeter
                0.0, // Double neuroretinalRimArea
                0.0, // Double excavationPerimeter
                0.0, // Double excavationArea
                "active", // String state
                0 // int ddlStage
        );
        String token = "Bearer valid_token";
        String ophtalId = "ophtal_id";

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);

        // Act
        ResponseEntity<?> response = chController.saveExam(token, examRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Exam saved successfully", response.getBody());
    }

    
    @Test
    public void testGetExamsByPacient_Success() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String pacientId = "123456";
        String ophtalId = "ophtal_id";
        List<ExamsResponse> exams = Collections.singletonList(new ExamsResponse(UUID.randomUUID(), // UUID examId
                "Exam Name", // String name
                "2023-10-31", // String date
                "http://example.com/image.png" // String urlImage
        ));

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);
        when(examService.getExamsByPacient(ophtalId, pacientId, 0, 10)).thenReturn(exams);

        // Act
        ResponseEntity<?> response = chController.getExamsByPacient(token, pacientId, 0, 10);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(exams, response.getBody());
    }

    

    @Test
    public void testDeleteExam_Success() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String examId = "exam_id";
        String pacientId = "pacient_id";
        String ophtalId = "ophtal_id";

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);

        // Act
        ResponseEntity<?> response = chController.deleteExam(token, examId,
                pacientId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Exam deleted successfully", response.getBody());
    }

    

    @Test
    public void testGetExam_Success() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String examId = "exam_id";
        String pacientId = "pacient_id";
        String ophtalId = "ophtal_id";
        ExamRes examRes = new ExamRes(UUID.randomUUID(), // UUID examId
                "Exam Name", // String name
                "2023-10-31", // String date
                "http://example.com/image.png", // String urlImage
                0.0, // Double distanceRatio
                0.0, // Double perimeterRatio
                0.0, // Double areaRatio
                0.0, // Double neuroretinalRimPerimeter
                0.0, // Double neuroretinalRimArea
                0.0, // Double excavationPerimeter
                0.0, // Double excavationArea
                "active", // String state
                0 // int ddlStage
        );

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);
        when(examService.getExamById(ophtalId, pacientId,
                examId)).thenReturn(examRes);

        // Act
        ResponseEntity<?> response = chController.getExam(token, examId, pacientId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(examRes, response.getBody());
    }

    

    @Test
    public void testGetExam_NotFound() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String examId = "exam_id";
        String pacientId = "pacient_id";
        String ophtalId = "ophtal_id";

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);
        when(examService.getExamById(ophtalId, pacientId, examId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = chController.getExam(token, examId, pacientId);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Exam not found", response.getBody());
    }

    

    @Test
    public void testSavePacient_Unauthorized() throws Exception {
        // Arrange
        PacientRequest pacient = new PacientRequest("", "");
        String token = "Bearer invalid_token";

        // Mock the behavior to throw UnauthorizedException when extracting the token
        try {
            jwtUtil.extractIdFromToken(token);
        } catch (UnauthorizedException e) {
            // Invalid token exception
            throw new UnauthorizedException("Invalid token");
        }

        ResponseEntity<?> response;
        // Act & Assert
        // Call the method and expect the correct handling of the exception
        try {
            response = chController.savePacient(token, pacient);
        } catch (UnauthorizedException e) {
            response = ResponseEntity.status(401).body(new ErrorResponse("Unauthorized", e.getMessage()));
        }

        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid Token or ophthalmologist ID not found.",
                ((ErrorResponse) response.getBody()).getMessage());
    }

    

    @Test
    public void testGetPacient_BadRequest() throws Exception {
        // Arrange
        String token = "Bearer valid_token";
        String cedula = "invalid_cedula";
        String ophtalId = "ophtal_id";

        when(jwtUtil.extractIdFromToken(token)).thenReturn(ophtalId);
        doThrow(new IllegalArgumentException("Bad Request")).when(pacientService).getPacientById(ophtalId,
                cedula);

        // Act
        ResponseEntity<?> response = chController.getPacient(token, cedula);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Bad Request", ((ErrorResponse) response.getBody()).getMessage());
    }
    
}
