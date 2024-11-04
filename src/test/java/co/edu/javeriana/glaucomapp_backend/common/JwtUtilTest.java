package co.edu.javeriana.glaucomapp_backend.common;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkey"; // Clave secreta de ejemplo
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil();
        jwtUtil.secretKey = SECRET_KEY; // Configurar la clave secreta para la prueba
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("email@example.com", claims.get("email"));
        assertEquals("USER", claims.get("role"));
        assertEquals("1", claims.getSubject());
    }

    
    @Test
    void testValidateToken_Invalid() {
        String token = "invalid.token.here";
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractEmail() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertEquals("email@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_Expired() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "email@example.com");
        claims.put("role", "USER");

        // Ensure to set claims correctly
        String token =

                Jwts.builder()
                        .claims().add(claims).and()
                        .subject("1")
                        .issuedAt(new Date(System.currentTimeMillis() - EXPIRATION_TIME)) // Token issued more than 1
                                                                                          // hour ago
                        .expiration(new Date(System.currentTimeMillis() - 1000)) // Token expired
                        .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY))) // Specify algorithm
                        .compact();

        assertTrue(jwtUtil.validateToken((token)));
    }

    @Test
    void testIsTokenValid_Valid() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        when(userDetails.getUsername()).thenReturn("email@example.com");
        assertTrue(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_Invalid() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        when(userDetails.getUsername()).thenReturn("different@example.com");
        assertFalse(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void testExtractSubject() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertEquals("1", jwtUtil.extractSubject(token));
    }

    @Test
    void testExtractRole() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertEquals("USER", jwtUtil.extractRole(token));
    }

    @Test
    void testAddToBlacklist() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "MOBILE");
        jwtUtil.addToBlacklist(token);
        assertTrue(jwtUtil.isBlacklisted(token));
    }

    @Test
    void testIsBlacklisted() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "MOBILE");
        assertFalse(jwtUtil.isBlacklisted(token));
        jwtUtil.addToBlacklist(token);
        assertTrue(jwtUtil.isBlacklisted(token));
    }

    @Test
    void testInvalidateToken() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "MOBILE");
        assertTrue(jwtUtil.invalidateToken("Bearer " + token));
        assertTrue(jwtUtil.isBlacklisted(token));
    }

    @Test
    void testExtractIdFromToken() {
        // Arrange
        MyUser userDetails = MyUser.builder()
                .id(UUID.randomUUID()) // This generates a random UUID
                .name("name")
                .username("example.com")
                .password("password")
                .role("MOBILE")
                .build();

        // Generate the token
        String token = jwtUtil.generateToken(userDetails);

        // Act
        String extractedId = jwtUtil.extractIdFromToken("Bearer " + token);

        // Assert
        assertEquals(userDetails.getId().toString(), extractedId); // Compare against the actual UUID string
    }


    @Test
    void testRefreshToken() {
        MyUser userDetails = MyUser.builder().id(UUID.randomUUID()).name("name").username("example.com")
                .password("password").role("MOBILE")
                .build();
        String expiredToken = jwtUtil.generateRefreshToken(userDetails);
        String refreshedToken = jwtUtil.refreshToken(expiredToken);
        assertNotNull(refreshedToken);
    }

    @Test
void testIsTokenValid_ValidToken() {
    String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
    // Simula que el token no está en la lista negra y no ha expirado
    assertTrue(jwtUtil.isTokenValid(token));
}

@Test
void testIsTokenValid_BlacklistedToken() {
    String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
    jwtUtil.addToBlacklist(token); // Agregar a la lista negra
    assertFalse(jwtUtil.isTokenValid(token)); // Debe ser inválido
}

@Test
void testIsTokenValid_ExpiredToken() {
    // Crear un token expirado manualmente
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", "email@example.com");
    claims.put("role", "USER");
    
    String token = Jwts.builder()
            .claims(claims)
            .subject("1")
            .issuedAt(new Date(System.currentTimeMillis() - EXPIRATION_TIME)) // Token emitido hace más de 1 hora
            .expiration(new Date(System.currentTimeMillis() - 1000)) // Token expirado
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY)))
            .compact();

    assertFalse(jwtUtil.isTokenValid(token)); // Debe ser inválido
}

@Test
void testExtractRefreshFromToken() {
    // Generar un token con el claim "refresh"
    String token = Jwts.builder()
            .claims(new HashMap<>())
            .subject("1")
            .claim("refresh", true) // Agregar el claim "refresh"
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY)))
            .compact();

    Boolean isRefresh = jwtUtil.extractRefreshFromToken("Bearer " + token);
    assertTrue(isRefresh); // Debe devolver true
}
}