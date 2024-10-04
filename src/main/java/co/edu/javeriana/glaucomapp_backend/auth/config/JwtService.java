/**
 * Service class for handling JWT (JSON Web Token) operations such as token generation, validation, and extraction of claims.
 * This class uses the io.jsonwebtoken library for JWT operations.
 * 
 * Configuration properties:
 * - jwt.secret: The secret key used for signing the JWT.
 * - jwt.expiration: The expiration time of the JWT in milliseconds.
 * 
 * Methods:
 * - generateToken(MyUser userDetails): Generates a JWT for the given user details with custom claims.
 * - createToken(Map<String, Object> claims, String subject): Private method to construct the JWT with the given claims and subject.
 * - generateKey(): Generates the secret key in SecretKey format from the base64 encoded secret key.
 * - extractUsername(String jwt): Extracts the username (subject) from the given JWT.
 * - getClaims(String jwt): Retrieves the claims from the given JWT.
 * - isTokenValid(String jwt): Validates if the given JWT is valid (not expired).
 * - isTokenExpired(Claims claims): Checks if the given JWT claims indicate an expired token.
 * 
 * Dependencies:
 * - org.springframework.beans.factory.annotation.Value: For injecting configuration properties.
 * - org.springframework.stereotype.Service: For marking this class as a Spring service.
 * - co.edu.javeriana.glaucomapp_backend.auth.model.MyUser: Custom user details model.
 * - io.jsonwebtoken.Claims: For handling JWT claims.
 * - io.jsonwebtoken.Jwts: For building and parsing JWTs.
 * - io.jsonwebtoken.security.Keys: For generating secret keys.
 * - lombok.Getter: For generating getter methods for the fields.
 */
package co.edu.javeriana.glaucomapp_backend.auth.config;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.model.MyUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;



@Getter
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    // Método para generar el JWT con claims personalizados
    public String generateToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "https://secure.genuinecoder.com");  // Añade un claim de emisor, puedes modificarlo
        return createToken(claims, userDetails.getUsername());
    }

    // Método privado para construir el JWT con los claims
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims().empty().add(claims).and()
                .subject(subject)  
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expirationTime)))
                .signWith(generateKey())  
                .compact();
    }

    // Método para generar la clave secreta en formato SecretKey
    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    // Extraer el nombre de usuario (subject) del token
    public String extractUsername(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }

    // Obtener los claims del JWT
    private Claims getClaims(String jwt) {
        return Jwts.parser()
                 .verifyWith(generateKey())
                 .build()
                 .parseSignedClaims(jwt)
                 .getPayload();
    }

    // Método para validar si el token es válido (no expirado)
    public boolean isTokenValid(String jwt) {
        Claims claims = getClaims(jwt);
        return !isTokenExpired(claims);
    }

    // Verifica si el token ha expirado
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(Date.from(Instant.now()));
    }
}
