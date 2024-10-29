package co.edu.javeriana.glaucomapp_backend.common;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    String secretKey; // Inyectar la clave secreta desde el archivo de propiedades

    private static final long ACCESS_TOKEN_VALIDITY = 1 * 60 * 1000;// 60 * 60 * 1000; // 60 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 2 * 60 * 1000; // 30 * 24 * 60 * 60 * 1000; // 30 days

    private final Set<String> blacklist = new HashSet<>();

    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }

    // Método para generar el JWT
    public String generateToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        return createToken(claims, userId);
    }

    public String createToken(Map<String, Object> claims, Long userId) {
        try {
            // Fecha de emisión y expiración
            long issuedAtEpoch = System.currentTimeMillis(); // Tiempo actual en milisegundos
            long expirationEpoch = issuedAtEpoch + 1000 * 60 * 60; // Expira en 1 hora

            // Generar el token JWT
            return Jwts.builder()
                    .claims().add(claims).and()

                    .subject(userId.toString())
                    .issuedAt(new Date(issuedAtEpoch))
                    .expiration(new Date(expirationEpoch))
                    .signWith(generateKey())
                    .compact();

        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        if (isBlacklisted(token)) {
            return false;
        }
        try {
            return !isTokenExpired(token); // Compara con la fecha actual
        } catch (ExpiredJwtException e) {
            System.out.println("Token is expired: " + e.getMessage());
            return true; // Si ya ha expirado, devolver true
        } catch (SignatureException e) {
            // Handle signature issues
            System.out.println("Invalid token signature: " + e.getMessage());
            return false; // Invalid token signature
        } catch (Exception e) {
            return false; // Manejar otras excepciones
        }

    }

    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }

    // Verificar si el token está expirado
    public Boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Extraer el email del JWT
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // Method to validate if the token is valid (web)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Method to validate if the token is valid (mobile)
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

    public String extractSubject(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }

    // extraer rol
    public String extractRole(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.get("role", String.class);
    }

    // Obtain the claims of the JWT
    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    /**
     * this is the start of Just Mobile methods
     * 
     * @param userDetails
     * @return
     */
    public String generateToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId()); // ofthalmologist id
        claims.put("name", userDetails.getName()); // ofthalmologist name
        return createToken(claims, userDetails.getUsername(), ACCESS_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId()); // ofthalmologist id
        claims.put("name", userDetails.getName()); // ofthalmologist name
        claims.put("refresh", true);
        return createToken(claims, userDetails.getUsername(), REFRESH_TOKEN_VALIDITY);
    }

    // Mobile method
    private String createToken(Map<String, Object> claims, String subject, long validity) {
        return Jwts.builder()
                .claims().empty().add(claims).and()
                .subject(subject)
                .issuedAt(Date.from(Instant.now()))
                .expiration(new Date(System.currentTimeMillis() + validity))
                .signWith(generateKey())
                .compact();
    }

    public String extractIdFromToken(String jwt) {
        try {// Trim whitespace from the token
            jwt = jwt.substring(7).trim();
            System.out.println("JWT Token on JwtUtil: " + jwt);

            if (validateToken(jwt)) {
                // Extract the ID from the claims
                System.out.println("Claims on JwtUtil: " + getClaims(jwt));
                Claims claims = getClaims(jwt);
                System.out.println("id on JwtUtil: " + claims.get("id", String.class));
                System.out.println("refresh on JwtUtil: " + claims.get("refresh", Boolean.class));
                String id = claims.get("id", String.class);
                System.out.println("ID on JwtUtil: " + id);
                return id;
            }
        } catch (ExpiredJwtException e) {
            System.out.println("Expired token: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error extracting ID: " + e.getMessage());
        }
        return null; // Handle invalid token case
    }

    public Boolean extractRefreshFromToken(String jwt) {
        // Trim whitespace from the token
        jwt = jwt.substring(7).trim();
        Claims claims = getClaims(jwt);
        return claims.get("refresh", Boolean.class);
    }

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

    public Boolean invalidateToken(String jwt) {
        // Trim whitespace from the token
        jwt = jwt.substring(7).trim();
        if (validateToken(jwt)) {
            addToBlacklist(jwt);
            return true;
        }
        return false; // Token is invalid or already blacklisted

    }

    public String refreshToken(String expiredToken) {
        try {
            // Intenta obtener los claims del token expirado
            Claims claims = Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(expiredToken)
                    .getPayload();

            // Si el token está expirado, los claims no se recuperarán aquí
            return null;
        } catch (ExpiredJwtException e) {
            // Extrae los claims del token expirado
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
}
