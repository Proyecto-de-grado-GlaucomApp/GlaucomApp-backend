/**
 * Service class that handles events related to ophthalmologists.
 * This class listens for events and processes them accordingly.
 * 
 * The event handler method listens for events with an ophthalmologist ID,
 * converts the ID to a UUID, and attempts to find the corresponding user
 * in the repository. If the user is not found, an IllegalArgumentException
 * is thrown.
 * 
 * Dependencies:
 * - MyUserRepository: Repository for accessing user data.
 * 
 * Annotations:
 * - @Service: Indicates that this class is a Spring service.
 * - @RequiredArgsConstructor: Generates a constructor with required arguments.
 * - @ApplicationModuleListener: Marks the method as an event listener.
 * 
 * Methods:
 * - void on(String ophtalId): Handles events with the given ophthalmologist ID.
 * 
 * @param ophtalId The ID of the ophthalmologist as a String.
 * @throws IllegalArgumentException if the ophthalmologist ID is invalid.
 */
package co.edu.javeriana.glaucomapp_backend.mobileauth.service;

import java.util.UUID;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.mobileauth.repository.MyUserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OphtalEventHandler {

    private final MyUserRepository userRepository;

    @ApplicationModuleListener
    void on(String ophtalId){
            UUID ophtalUUID = UUID.fromString(ophtalId);
            userRepository.findById(ophtalUUID)
            .orElseThrow(() -> new IllegalArgumentException("Invalid Ophthalmologist ID"));
        }
    
}
