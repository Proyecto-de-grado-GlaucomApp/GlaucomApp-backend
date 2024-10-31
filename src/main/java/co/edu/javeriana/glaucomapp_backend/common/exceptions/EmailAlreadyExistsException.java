package co.edu.javeriana.glaucomapp_backend.common.exceptions;

/**
 * Exception thrown when an attempt is made to create an API key for an email 
 * that is already associated with an existing API key.
 * <p>
 * This runtime exception indicates that the requested operation cannot be 
 * completed because the email address provided is already in use.
 * </p>
 */
public class EmailAlreadyExistsException extends RuntimeException {
    
    /**
     * Constructs a new EmailAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
