/**
 * Service class for handling events related to doctors.
 * This service is responsible for publishing events when certain actions are performed.
 * 
 * 
 * This class uses {@link ApplicationEventPublisher} to publish events.
 * 
 * 
 * The {@code deletePatient} method is marked as {@code @Transactional} to ensure
 * that the operation is executed within a transaction context.
 * 
 * 
 */
package co.edu.javeriana.glaucomapp_backend.mobileauth.repository;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorEventService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void deletePatient(UUID ophtalmologistId){
        
        eventPublisher.publishEvent(ophtalmologistId);
    }

}
