package co.edu.javeriana.glaucomapp_backend.security.filter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JwtAuthenticationFilterWebTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private JwtAuthenticationFilterWeb jwtAuthenticationFilterWeb;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // Limpia el contexto de seguridad
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes the mock objects
    
        jwtAuthenticationFilterWeb = new JwtAuthenticationFilterWeb(jwtUtil);

        // Mock the PrintWriter to avoid NullPointerException
        try {
            PrintWriter writer = mock(PrintWriter.class);
            when(response.getWriter()).thenReturn(writer);
        } catch (IOException e) {
            fail("Could not mock response writer");
        }
    }

    @Test
    void testDoFilterInternal_ValidJwt() throws ServletException, IOException {
        String token = "validToken";
        String username = "user@example.com";
        String role = "ROLE_USER";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenReturn(username);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractRole(token)).thenReturn(role);

        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidJwt() throws ServletException, IOException {
        String[] tokens = {
            "expiredToken",
            "unsupportedToken",
            "malformedToken",
            "invalidToken"
        };
        
        for (String token : tokens) {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

            if (token.equals("expiredToken")) {
                when(jwtUtil.extractEmail(token)).thenThrow(new ExpiredJwtException(null, null, "Token has expired"));
                verifyResponseStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }

    @Test
    void testDoFilterInternal_MissingJwt() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);
    }

    private void verifyResponseStatus(int expectedStatus) throws IOException, ServletException {
        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);
        verify(response, times(1)).setStatus(expectedStatus);
        verify(chain, never()).doFilter(request, response);
    }
}
