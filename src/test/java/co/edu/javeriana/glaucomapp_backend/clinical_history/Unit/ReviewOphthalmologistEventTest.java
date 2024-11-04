package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit;

import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ReviewOphthalmologistEvent;



public class ReviewOphthalmologistEventTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewOphthalmologistEvent reviewOphthalmologistEvent;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testVerifyOphtalmologist() {
        String ophtalId = "testId";

        reviewOphthalmologistEvent.verifyOphtalmologist(ophtalId);

        verify(eventPublisher).publishEvent(ophtalId);
    }
}