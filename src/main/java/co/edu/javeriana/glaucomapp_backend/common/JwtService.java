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
package co.edu.javeriana.glaucomapp_backend.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;



@Getter
@Service
public class JwtService {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    /*
    // Mehtod to generate the JWT token
    public String generateToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());  // ofthalmologist id
        claims.put("name", userDetails.getName());  // ofthalmologist name
        return createToken(claims, userDetails.getUsername());
    }

    // Privet method to create the JWT token
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims().empty().add(claims).and()
                .subject(subject)  
                .issuedAt(Date.from(Instant.now()))
                .signWith(generateKey())  
                .compact();
    }

    //Method to generate the secret key in SecretKey format
    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    // Extract the username (subject) from the token
    public String extractUsername(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }

    //Obtain the claims of the JWT
    private Claims getClaims(String jwt) {
        return Jwts.parser()
                 .verifyWith(generateKey())
                 .build()
                 .parseSignedClaims(jwt)
                 .getPayload();
    }

     // Method to validate if the token is valid
     public boolean isTokenValid(String jwt) {
        try {
            getClaims(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

     */
}
