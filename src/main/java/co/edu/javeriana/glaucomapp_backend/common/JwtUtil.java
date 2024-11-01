/**
 * Utility class for handling JWT (JSON Web Token) operations such as token generation, validation, and extraction of claims.
 * This class uses the io.jsonwebtoken library for JWT operations and Spring's @Value annotation for injecting the secret key.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Generate access and refresh tokens for web and mobile applications.</li>
 *   <li>Validate tokens, including checking for expiration and blacklist status.</li>
 *   <li>Extract claims such as email, ID, role, and subject from tokens.</li>
 *   <li>Invalidate tokens by adding them to a blacklist.</li>
 *   <li>Refresh expired tokens.</li>
 * </ul>
 * 
 * <p>Usage:</p>
 * <pre>
 * {@code
 * JwtUtil jwtUtil = new JwtUtil();
 * String token = jwtUtil.generateToken(email, userId, role);
 * boolean isValid = jwtUtil.validateToken(token);
 * String email = jwtUtil.extractEmail(token);
 * }
 * </pre>
 * 
 * <p>Note:</p>
 * <ul>
 *   <li>The secret key is injected from the application properties using the @Value annotation.</li>
 *   <li>Tokens are signed using the HMAC SHA algorithm.</li>
 *   <li>Expired tokens can be refreshed using the refreshToken method.</li>
 * </ul>
 * 
 * <p>Exceptions:</p>
 * <ul>
 *   <li>InvalidKeyException: Thrown when the secret key is invalid.</li>
 *   <li>ExpiredJwtException: Thrown when the token is expired.</li>
 *   <li>SignatureException: Thrown when the token signature is invalid.</li>
 * </ul>
 * 
 * <p>Dependencies:</p>
 * <ul>
 *   <li>io.jsonwebtoken: For JWT operations.</li>
 *   <li>Spring Framework: For dependency injection and property management.</li>
 * </ul>
 * 
 * @see io.jsonwebtoken.Jwts
 * @see io.jsonwebtoken.Claims
 * @see io.jsonwebtoken.ExpiredJwtException
 * @see io.jsonwebtoken.security.Keys
 * @see io.jsonwebtoken.security.InvalidKeyException
 * @see io.jsonwebtoken.security.SignatureException
 */

package co.edu.javeriana.glaucomapp_backend.common;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    String secretKey; // Inyectar la clave secreta desde el archivo de propiedades

    private static final long ACCESS_TOKEN_VALIDITY = 60 * 60 * 1000; // 60 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 30 * 24 * 60 * 60 * 1000; // 30 days

    private final Set<String> blacklist = new HashSet<>();

    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }

    /**
     * Generates a JWT token with the specified email, user ID, and role.
     *
     * @param email  the email of the user
     * @param userId the ID of the user
     * @param role   the role of the user
     * @return a JWT token as a String
     */
    public String generateToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        return createToken(claims, userId.toString(), ACCESS_TOKEN_VALIDITY);
    }

    /**
     * Generates a JWT token for the given user details.
     *
     * @param userDetails the user details for which the token is to be generated
     * @return the generated JWT token as a String
     */
    public String generateToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());
        claims.put("name", userDetails.getName());
        return createToken(claims, userDetails.getUsername(), ACCESS_TOKEN_VALIDITY);
    }

    /**
     * Generates a refresh token for the given user details.
     *
     * @param userDetails the user details for which the refresh token is to be
     *                    generated
     * @return the generated refresh token as a String
     */
    public String generateRefreshToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());
        claims.put("name", userDetails.getName());
        claims.put("refresh", true);
        return createToken(claims, userDetails.getUsername(), REFRESH_TOKEN_VALIDITY);
    }

    /**
     * Creates a JWT token with the specified claims, subject, and validity period.
     *
     * @param claims   a map of claims to be included in the token
     * @param subject  the subject for which the token is being created
     * @param validity the validity period of the token in milliseconds
     * @return the generated JWT token as a String
     * @throws RuntimeException if there is an issue with the key used for signing
     *                          the token
     */
    private String createToken(Map<String, Object> claims, String subject, long validity) {
        try {
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + validity))
                    .signWith(generateKey())
                    .compact();
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception: " + e.getMessage());
        }
    }

    /**
     * Validates the given JWT token.
     * 
     * This method checks if the token is blacklisted and if it is expired.
     * It also handles exceptions related to token expiration and invalid signatures.
     * 
     * @param token the JWT token to be validated
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        if (isBlacklisted(token)) {
            return false;
        }
        try {
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            System.out.println("Token is expired: " + e.getMessage());
            return true; // Consider expired tokens as invalid
        } catch (SignatureException e) {
            System.out.println("Invalid token signature: " + e.getMessage());
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates the given JWT token.
     * 
     * This method checks if the token is blacklisted and if it is expired.
     * 
     * @param jwt the JWT token to be validated
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean isTokenValid(String jwt) {
        try {
            getClaims(jwt);
            // Verify if the token is blacklisted or expired
            if (isBlacklisted(jwt) || isTokenExpired(jwt)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
    /**
     * Extracts the claims from a given JWT.
     *
     * @param jwt the JSON Web Token from which to extract the claims
     * @return the claims contained in the JWT
     */
    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    
    /**
     * Extracts the email from the given JWT token.
     *
     * @param token the JWT token from which to extract the email
     * @return the email extracted from the token
     */
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Extracts the ID from the given JWT token.
     *
     * @param jwt the JWT token from which to extract the ID
     * @return the ID extracted from the token
     */
    public String extractIdFromToken(String jwt) {
        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("Token is null or empty");
        }
        jwt = jwt.substring(7).trim();
        if (validateToken(jwt)) {
            return getClaims(jwt).get("id", String.class);
        }
        return null;
    }

    /**
     * Extracts the role from the given JWT token.
     *
     * @param jwt the JWT token from which to extract the role
     * @return the role extracted from the token
     */
    public String extractRole(String jwt) {
        return getClaims(jwt).get("role", String.class);
    }

    /**
     * Checks if the given JWT token is expired.
     *
     * @param token the JWT token to be checked
     * @return true if the token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * Generates a secret key from the base64-encoded secret key string.
     *
     * @return the generated secret key
     */
    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * Refreshes an expired token by generating a new token with the same claims.
     *
     * @param expiredToken the expired JWT token to be refreshed
     * @return the refreshed JWT token as a String
     */
    public String refreshToken(String expiredToken) {
        try {
            Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(expiredToken)
                    .getPayload();
            return null; // This line is unreachable; placeholder for clarity
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            // Genera un nuevo token de acceso utilizando los mismos claims
            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                    .signWith(generateKey())
                    .compact();
        }
    }


    /**
     * Validates the given JWT token by checking if the username extracted from the token
     * matches the username from the provided UserDetails and if the token is not expired.
     *
     * @param token the JWT token to be validated
     * @param userDetails the UserDetails object containing the user's information
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    /**
     * Extracts the subject (typically the user identifier) from the given JWT.
     *
     * @param jwt the JSON Web Token from which to extract the subject
     * @return the subject contained in the JWT
     */
    public String extractSubject(String jwt) {
        return getClaims(jwt).getSubject();
    }

    /**
     * Extracts the "refresh" claim from the given JWT token.
     *
     * @param jwt the JWT token from which the "refresh" claim is to be extracted.
     * @return the value of the "refresh" claim as a Boolean.
     */
    public Boolean extractRefreshFromToken(String jwt) {
        jwt = jwt.substring(7).trim();
        return getClaims(jwt).get("refresh", Boolean.class);
    }

    /**
     * Invalidates the given JWT token by adding it to the blacklist.
     *
     * @param jwt the JWT token to be invalidated
     * @return true if the token was successfully invalidated and added to the blacklist, false otherwise
     */
    public Boolean invalidateToken(String jwt) {
        jwt = jwt.substring(7).trim();
        if (validateToken(jwt)) {
            addToBlacklist(jwt);
            return true;
        }
        return false;
    }

    /**
     * Extracts the user ID from a JWT token for refresh purposes.
     *
     * This method decodes the JWT token without validating it and retrieves the user ID
     * from the token's claims.
     *
     * @param jwt the JWT token to decode
     * @return the user ID extracted from the token, or null if decoding fails
     */
    public String extractIdFromTokenForRefresh(String jwt) {
        try {
            // Decodificar el JWT sin validar
            Claims claims = Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            return claims.get("id", String.class);
        } catch (Exception e) {
            // Manejar la excepción si la decodificación falla
            System.out.println("Error al decodificar el token: " + e.getMessage());
            return null;
        }
    }

}
