/**
 * Service class for handling ophthalmologist review events.
 * This class is responsible for publishing events related to the verification of ophthalmologists.
 * 
 * It uses the {@link ApplicationEventPublisher} to publish events.
 * 
 * This class is annotated with {@link Service} to indicate that it's a Spring service component,
 * and {@link RequiredArgsConstructor} to generate a constructor with required arguments.
 * 
 * The {@link Transactional} annotation is used to ensure that the method is executed within a transaction context.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.service;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewOphthalmologistEvent {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void verifyOphtalmologist(String ophtalId){
        
        eventPublisher.publishEvent(ophtalId);
    }
    
}
