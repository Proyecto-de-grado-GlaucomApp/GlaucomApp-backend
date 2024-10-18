package co.edu.javeriana.glaucomapp_backend.common;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date(issuedAtEpoch))
                    .setExpiration(new Date(expirationEpoch))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();

        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception: " + e.getMessage());
        }
    }

public boolean validateToken(String token) {
    // 1. Verificar la firma del token y extraer los claims
    Claims claims;
    try {
        claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    } catch (JwtException e) { 
        // El token no es válido
        return false;
    }

    // 2. Verificar la expiración
    if (isTokenExpired(claims.getExpiration())) {
        return false; // Token expirado
    }
    return true; // Token válido
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
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody(); // Usar la clave secreta inyectada
    }

    // Verificar si el token está expirado
    public Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Convertir la clave secreta a un objeto Key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String extractSubject(String jwt) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(jwt).getBody().getSubject();
    }

    //extraer rol
    public String extractRole(String jwt) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(jwt).getBody().get("role", String.class);
    }

}
