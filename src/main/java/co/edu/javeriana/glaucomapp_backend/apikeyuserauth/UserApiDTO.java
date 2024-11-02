package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

public record UserApiDTO(
    String email,
    String entity,
    String plainPassword,
    String contactName
) {
}
