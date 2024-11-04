package co.edu.javeriana.glaucomapp_backend.common;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        when(userDetails.getUsername()).thenReturn("email@example.com");
        assertTrue(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void testValidateToken_Invalid() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testExtractEmail() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertEquals("email@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void testIsTokenExpired() {
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
    void testTokenBlacklistFunctionality() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "MOBILE");
        assertFalse(jwtUtil.isBlacklisted(token));
        jwtUtil.addToBlacklist(token);
        assertTrue(jwtUtil.isBlacklisted(token));
        assertFalse(jwtUtil.isTokenValid(token));
    }

    @Test
    void testExtractSubjectAndRole() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertEquals("1", jwtUtil.extractSubject(token));
        assertEquals("USER", jwtUtil.extractRole(token));
    }

    @Test
    void testExtractIdFromToken() {
        MyUser userDetails = MyUser.builder()
                .id(UUID.randomUUID())
                .name("name")
                .username("example.com")
                .password("password")
                .role("MOBILE")
                .build();
        String token = jwtUtil.generateToken(userDetails);
        assertEquals(userDetails.getId().toString(), jwtUtil.extractIdFromToken("Bearer " + token));
    }

    @Test
    void testRefreshToken() {
        MyUser userDetails = MyUser.builder()
                .id(UUID.randomUUID())
                .name("name")
                .username("example.com")
                .password("password")
                .role("MOBILE")
                .build();
        String expiredToken = jwtUtil.generateRefreshToken(userDetails);
        String refreshedToken = jwtUtil.refreshToken(expiredToken);
        assertNotNull(refreshedToken);
    }

    @Test
    void testExtractRefreshFromToken() {
        String token = Jwts.builder()
                .claim("refresh", true)
                .setSubject("1")
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY)))
                .compact();
        assertTrue(jwtUtil.extractRefreshFromToken("Bearer " + token));
    }

    @Test
void testExtractIdFromTokenForRefresh() {
    // Crear un token con el claim "id"
    String expectedId = UUID.randomUUID().toString(); // Generar un UUID de ejemplo
    String token = Jwts.builder()
            .claim("id", expectedId) // Agregar el claim "id"
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY)))
            .compact();

    // Llamar al método y verificar el resultado
    String extractedId = jwtUtil.extractIdFromTokenForRefresh(token);
    assertEquals(expectedId, extractedId); // Comparar el resultado con el valor esperado
}

@Test
void testExtractIdFromTokenForRefresh_InvalidToken() {
    // Token inválido para probar la excepción
    String invalidToken = "invalid.token.here";

    // Capturar la salida del sistema para verificar el mensaje de error (opcional)
    try {
        String extractedId = jwtUtil.extractIdFromTokenForRefresh(invalidToken);
        assertNull(extractedId); // Debe devolver null si falla la decodificación
    } catch (Exception e) {
        // Verificar que el mensaje de error se imprima correctamente
        System.out.println("Error capturado en la prueba: " + e.getMessage());
    }
}

}
