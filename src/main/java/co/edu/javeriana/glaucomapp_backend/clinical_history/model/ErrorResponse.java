package co.edu.javeriana.glaucomapp_backend.clinical_history.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an error response with a status and a message.
 * This class is used to encapsulate error details that can be sent back to the client.
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String status;
    private String message;
    
}
