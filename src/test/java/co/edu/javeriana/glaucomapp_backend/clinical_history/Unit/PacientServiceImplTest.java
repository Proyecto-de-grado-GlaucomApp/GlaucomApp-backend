package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ReviewOphthalmologistEvent;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl.PacientServiceImpl;
import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;

class PacientServiceImplTest {

    @Mock
    private PacientRepository pacientRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ReviewOphthalmologistEvent reviewOphthalmologistEvent;

    @InjectMocks
    private PacientServiceImpl pacientServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeletePacient() {
        UUID pacientId = UUID.randomUUID();
        Pacient pacient = mock(Pacient.class);
        Exam exam = mock(Exam.class); // Create a mock Exam
        List<Exam> exams = List.of(exam); // Use the mock Exam in the list

        // Mock the behavior of the repositories and the Pacient object
        when(pacientRepository.findById(pacientId)).thenReturn(Optional.of(pacient));
        when(pacient.getExams()).thenReturn(exams);
        when(exam.getUrlImage()).thenReturn("image.png"); // Return a valid URL image

        // Call the method to be tested
        pacientServiceImpl.deletePacient(pacientId);

        // Verify the interactions
        verify(s3Service).deleteImage("image.png"); // Check that the correct image was deleted
        verify(examRepository).deleteAll(exams); // Ensure exams are deleted
        verify(pacientRepository).delete(pacient); // Ensure the pacient is deleted
    }

    @Test
    void testDeletePacientNotFound() {
        UUID pacientId = UUID.randomUUID();

        when(pacientRepository.findById(pacientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> pacientServiceImpl.deletePacient(pacientId));
    }

    @Test
    void testSavePacient() {
        PacientRequest pacientRequest = new PacientRequest("123", "John Doe");
        String ophtalIdString = UUID.randomUUID().toString();
        UUID ophtalId = UUID.fromString(ophtalIdString);

        when(pacientRepository.findPacientByCedulaAndDoctorId(pacientRequest.cedula(), ophtalId)).thenReturn(null);

        pacientServiceImpl.savePacient(pacientRequest, ophtalIdString);

        verify(pacientRepository).save(any(Pacient.class));
    }

    @Test
    void testSavePacientCedulaExists() {
        PacientRequest pacientRequest = new PacientRequest("123", "John Doe");
        String ophtalIdString = UUID.randomUUID().toString();
        UUID ophtalId = UUID.fromString(ophtalIdString);

        when(pacientRepository.findPacientByCedulaAndDoctorId(pacientRequest.cedula(), ophtalId))
                .thenReturn(mock(Pacient.class));

        assertThrows(IllegalArgumentException.class,
                () -> pacientServiceImpl.savePacient(pacientRequest, ophtalIdString));
    }

    @Test
    void testGetPacientsByOphtal() {
        String ophtalIdString = UUID.randomUUID().toString();
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.randomUUID();
        List<Pacient> pacients = List.of(Pacient.builder().id(pacientId).name("John Doe").build());

        when(pacientRepository.findAllPacientsByDoctorId(ophtalId)).thenReturn(pacients);

        List<PacientResponse> responses = pacientServiceImpl.getPacientsByOphtal(ophtalIdString, 0, 1);

        assertEquals(1, responses.size());
        assertEquals("John Doe", responses.get(0).name());
    }

    @Test
    void testDeletePacientByOphtalAndPacientId() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        UUID ophtalId = UUID.fromString(ophtalIdString);
        UUID pacientId = UUID.fromString(pacientIdString);
        Pacient pacient = mock(Pacient.class);
        Exam exam = mock(Exam.class); // Create a mock Exam

        // Mocking the behavior of the repositories and the Pacient object
        when(pacientRepository.findById(pacientId)).thenReturn(Optional.of(pacient));
        when(pacient.getDoctorId()).thenReturn(ophtalId);

        // Use a mutable list for exams
        List<Exam> exams = new ArrayList<>();
        exams.add(exam);
        when(pacient.getExams()).thenReturn(exams); // Return the mutable list

        when(exam.getUrlImage()).thenReturn("image.png"); // Mock a valid URL image

        // Call the method to be tested
        pacientServiceImpl.deletePacient(ophtalIdString, pacientIdString);

        // Verify the interactions
        verify(s3Service).deleteImage("image.png"); // Check that the correct image was deleted
        verify(pacientRepository).delete(pacient); // Ensure the pacient is deleted
    }

    @Test
    void testDeletePacientByOphtalAndPacientIdUnauthorized() {
        String ophtalIdString = UUID.randomUUID().toString();
        String pacientIdString = UUID.randomUUID().toString();
        UUID pacientId = UUID.fromString(pacientIdString);
        Pacient pacient = mock(Pacient.class);

        when(pacientRepository.findById(pacientId)).thenReturn(Optional.of(pacient));
        when(pacient.getDoctorId()).thenReturn(UUID.randomUUID());

        assertThrows(AccessDeniedException.class,
                () -> pacientServiceImpl.deletePacient(ophtalIdString, pacientIdString));
    }

    @Test
    void testGetPacientById() {
        String ophtalIdString = UUID.randomUUID().toString();
        String cedula = "123";
        UUID ophtalId = UUID.fromString(ophtalIdString);
        Pacient pacient = Pacient.builder().id(UUID.randomUUID()).name("John Doe").cedula("123").build();

        when(pacientRepository.findPacientByCedulaAndDoctorId(cedula, ophtalId)).thenReturn(pacient);

        PacientResponse response = pacientServiceImpl.getPacientById(ophtalIdString, cedula);

        assertEquals("John Doe", response.name());
    }

    @Test
    void testGetPacientByIdNotFound() {
        String ophtalIdString = UUID.randomUUID().toString();
        String cedula = "123";
        UUID ophtalId = UUID.fromString(ophtalIdString);

        when(pacientRepository.findPacientByCedulaAndDoctorId(cedula, ophtalId)).thenReturn(null);

        PacientResponse response = pacientServiceImpl.getPacientById(ophtalIdString, cedula);

        assertNull(response.PacinetId());
        assertNull(response.name());
        assertNull(response.cedula());
    }
}