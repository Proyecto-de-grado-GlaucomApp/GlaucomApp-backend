package co.edu.javeriana.glaucomapp_backend.mobileauth.unit;

import static org.mockito.Mockito.verify;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.javeriana.glaucomapp_backend.mobileauth.service.DoctorEventService;


public class DoctorEventServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DoctorEventService doctorEventService;

    public DoctorEventServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDeletePatient() {
        UUID ophtalmologistId = UUID.randomUUID();

        doctorEventService.deletePatient(ophtalmologistId);

        verify(eventPublisher).publishEvent(ophtalmologistId);
    }
}