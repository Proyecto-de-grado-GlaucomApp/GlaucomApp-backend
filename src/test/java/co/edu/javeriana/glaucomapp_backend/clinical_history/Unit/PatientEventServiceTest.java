package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit;

import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.ExamRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.repository.PacientRepository;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl.PatientEventListener;
import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;

public class PatientEventServiceTest {

    @Mock
    private PacientRepository pacientRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PatientEventListener patientEventService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void invokePrivateMethod(String methodName, Object... args) throws Exception {
        Method method = PatientEventListener.class.getDeclaredMethod(methodName, UUID.class);
        method.setAccessible(true);
        method.invoke(patientEventService, args);
    }

    @Test
    public void testOn() throws Exception {
        UUID ophtalmologistId = UUID.randomUUID();
        List<Pacient> patients = new ArrayList<>();
        Pacient pacient = mock(Pacient.class);
        Exam exam = mock(Exam.class);
        List<Exam> exams = new ArrayList<>();
        exams.add(exam);
        when(pacient.getExams()).thenReturn(exams);
        when(exam.getUrlImage()).thenReturn("urlImage");
        patients.add(pacient);

        when(pacientRepository.findAllPacientsByDoctorId(ophtalmologistId)).thenReturn(patients);

        invokePrivateMethod("on", ophtalmologistId); // Invoke the private method

        verify(examRepository).deleteAll(exams);
        verify(pacientRepository).delete(pacient);
        verify(s3Service).deleteImage("urlImage");
    }

    @Test
    public void testOnNoPatients() throws Exception {
        UUID ophtalmologistId = UUID.randomUUID();
        List<Pacient> patients = new ArrayList<>();

        when(pacientRepository.findAllPacientsByDoctorId(ophtalmologistId)).thenReturn(patients);

        invokePrivateMethod("on", ophtalmologistId); // Invoke the private method

        verify(examRepository, never()).deleteAll(anyList());
        verify(pacientRepository, never()).delete(any(Pacient.class));
        verify(s3Service, never()).deleteImage(anyString());
    }
}
