package co.edu.javeriana.glaucomapp_backend.common.exceptions;

public class ApiKeyNotFoundException extends RuntimeException {
    
    public ApiKeyNotFoundException(String message) {
        super(message);
    }
}