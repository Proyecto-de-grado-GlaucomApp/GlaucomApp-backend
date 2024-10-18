package co.edu.javeriana.glaucomapp_backend.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;




public class JwtUtilTest {

    @Mock
    private UserDetails userDetails;

    private JwtUtil jwtUtil;

    @Value("${JWT_SECRET_KEY}")
    private String secretKey = "mysecretkeymysecretkeymysecretkeymysecretkey"; // Example secret key

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil();
        jwtUtil.secretKey = secretKey; // Manually set the secret key for testing
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertNotNull(token);

        Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseClaimsJws(token).getBody();
        assertEquals("email@example.com", claims.get("email"));
        assertEquals("USER", claims.get("role"));
        assertEquals("1", claims.getSubject());
    }

    @Test
    void testValidateToken_Valid() {
        String token = jwtUtil.generateToken("email@example.com", 1L, "USER");
        assertTrue(jwtUtil.validateToken(token));
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
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject("1")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) 
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) 
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
        

        assertFalse(jwtUtil.validateToken(token));
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
}