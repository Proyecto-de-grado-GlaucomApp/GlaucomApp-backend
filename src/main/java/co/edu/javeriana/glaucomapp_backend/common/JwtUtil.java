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

    private static final long ACCESS_TOKEN_VALIDITY = 1 * 60 * 1000; // 60 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 2 * 24 * 60 * 60 * 1000; // 30 days

    private final Set<String> blacklist = new HashSet<>();

    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }

    // Generate access token (web)
    public String generateToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        return createToken(claims, userId.toString(), ACCESS_TOKEN_VALIDITY);
    }

    // Generate access token (mobile)
    public String generateToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());
        claims.put("name", userDetails.getName());
        return createToken(claims, userDetails.getUsername(), ACCESS_TOKEN_VALIDITY);
    }

    // Generate refresh token (mobile)
    public String generateRefreshToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());
        claims.put("name", userDetails.getName());
        claims.put("refresh", true);
        return createToken(claims, userDetails.getUsername(), REFRESH_TOKEN_VALIDITY);
    }

    // General method to create tokens
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

    // Validate token and check if it's expired or blacklisted
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

    // Obtain the claims of the JWT
    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    // Extraer el email del JWT
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // Extract ID from token
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

    // Extract role from token
    public String extractRole(String jwt) {
        return getClaims(jwt).get("role", String.class);
    }

    // Check if token is expired
    public Boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Generate a secret key from the base64-encoded string
    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    // Refresh an expired token
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

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Extract subject from token
    public String extractSubject(String jwt) {
        return getClaims(jwt).getSubject();
    }

    // Extract refresh status from token
    public Boolean extractRefreshFromToken(String jwt) {
        jwt = jwt.substring(7).trim();
        return getClaims(jwt).get("refresh", Boolean.class);
    }

    // Invalidate a token by adding it to the blacklist
    public Boolean invalidateToken(String jwt) {
        jwt = jwt.substring(7).trim();
        if (validateToken(jwt)) {
            addToBlacklist(jwt);
            return true;
        }
        return false;
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

}
