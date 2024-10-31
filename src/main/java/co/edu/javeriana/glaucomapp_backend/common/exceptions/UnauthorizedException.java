package co.edu.javeriana.glaucomapp_backend.common.exceptions;


public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}