package co.edu.javeriana.glaucomapp_backend.auth.repository;

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
