package co.edu.javeriana.glaucomapp_backend.common;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

        public String generateToken(MyUser userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());  // ofthalmologist id
        claims.put("name", userDetails.getName());  // ofthalmologist name
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims().empty().add(claims).and()
                .subject(subject)  
                .issuedAt(Date.from(Instant.now()))
                .signWith(generateKey())  
                .compact();
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
    try {
        Date expiration = getClaims(token).getExpiration();
        return expiration.before(new Date()); // Compara con la fecha actual
    } catch (ExpiredJwtException e) {
        return true; // Si ya ha expirado, devolver true
    } catch (Exception e) {
        return false; // Manejar otras excepciones
    }

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
        return getClaims(token).get("email", String.class);
    }



    // Verificar si el token está expirado
    public Boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractSubject(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }

    //extraer rol
    public String extractRole(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.get("role", String.class);
    }




        //Obtain the claims of the JWT
        private Claims getClaims(String jwt) {
            return Jwts.parser()
                     .verifyWith(generateKey())
                     .build()
                     .parseSignedClaims(jwt)
                     .getPayload();
        }
        public String extractIdFromToken(String jwt) {
            // Trim whitespace from the token
            jwt = jwt.substring(7).trim();
            System.out.println("JWT Token on JwtUtil: " + jwt);
            
            if (validateTokenMobile(jwt)) {
                // Extract the ID from the claims
                System.out.println("Claims on JwtUtil: " + getClaims(jwt));
                Claims claims = getClaims(jwt);
                System.out.println("id on JwtUtil: " + claims.get("id", String.class));
                String id = claims.get("id", String.class);
                System.out.println("ID on JwtUtil: " + id);
                
                // Remove "Bearer " prefix
                return id;
            }
            return null; // Handle invalid token case
        }
        
    
        private boolean validateTokenMobile(String jwt) {
            return true;
        }
}
