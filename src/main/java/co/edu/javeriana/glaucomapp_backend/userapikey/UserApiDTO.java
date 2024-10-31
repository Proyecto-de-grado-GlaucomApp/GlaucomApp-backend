package co.edu.javeriana.glaucomapp_backend.userapikey;

public record UserApiDTO(
    String email,
    String entity,
    String plainPassword,
    String contactName
) {
}
