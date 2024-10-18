package co.edu.javeriana.glaucomapp_backend.apikey;

public enum ApiKeyStatus {
    ACTIVE,           // The API key is active and can be used
    INACTIVE,         // The API key is inactive and cannot be used
    PENDING  // The API key is waiting for admin approval
}
