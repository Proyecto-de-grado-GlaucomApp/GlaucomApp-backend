package co.edu.javeriana.glaucomapp_backend.common.exceptions;

public class ApiKeyAlreadyExistsException extends RuntimeException {
    public ApiKeyAlreadyExistsException(String message) {
        super(message);
    }

}
