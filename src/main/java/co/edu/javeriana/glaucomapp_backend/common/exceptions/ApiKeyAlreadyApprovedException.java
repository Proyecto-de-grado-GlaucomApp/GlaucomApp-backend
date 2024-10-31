package co.edu.javeriana.glaucomapp_backend.common.exceptions;

public class ApiKeyAlreadyApprovedException extends RuntimeException {
    public ApiKeyAlreadyApprovedException(String message) {
        super(message);
    }
}
