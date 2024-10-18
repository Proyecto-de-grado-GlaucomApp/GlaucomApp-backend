package co.edu.javeriana.glaucomapp_backend.common;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

   
    @Value("${JWT_SECRET_KEY}") String secretKey; // Inyectar la clave secreta desde el archivo de propiedades

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
    // 1. Verificar la firma del token y extraer los claims
    Claims claims;
    try {
        claims = Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    } catch (JwtException e) { 
        // El token no es válido
        return false;
    }

    // 2. Verificar la expiración
    return !isTokenExpired(claims.getExpiration()); // Token expirado
}


    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

public boolean isTokenExpired(Date expiration) {
    try {
        return expiration.before(new Date());
    } catch (ExpiredJwtException e) {
        return true; 
    } catch (Exception e) {
        return false;
    }
}



    // Extraer el email del JWT
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    // Extraer todos los claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                 .verifyWith(generateKey())
                 .build()
                 .parseSignedClaims(token)
                 .getPayload();
    }

    // Verificar si el token está expirado
    public Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    public String extractSubject(String jwt) {
        Claims claims = extractAllClaims(jwt);
        return claims.getSubject();
    }

    //extraer rol
    public String extractRole(String jwt) {
        Claims claims = extractAllClaims(jwt);
        return claims.get("role", String.class);
    }

}
