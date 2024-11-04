package co.edu.javeriana.glaucomapp_backend.security;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.security.filter.JwtAuthenticationFilterWeb;
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
    void testDoFilterInternal_ExpiredJwt() throws ServletException, IOException {
        String token = "expiredToken";
    
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenThrow(new ExpiredJwtException(null, null, "Token has expired"));
    
        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);
    
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Expecting 401
        verify(chain, never()).doFilter(request, response);
    }
    

    @Test
    void testDoFilterInternal_UnsupportedJwt() throws ServletException, IOException {
        String token = "unsupportedToken";
    
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenThrow(new UnsupportedJwtException("Unsupported token"));
    
        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);
    
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Esperando 401
        verify(chain, never()).doFilter(request, response);
    }
    

    @Test
    void testDoFilterInternal_MalformedJwt() throws ServletException, IOException {
        String token = "malformedToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenThrow(new MalformedJwtException("Malformed token"));

        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidTokenFormat() throws ServletException, IOException {
        String token = "invalidToken";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenThrow(new IllegalArgumentException("Invalid token format"));

        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_MissingJwt() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalidToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenThrow(new IllegalArgumentException("Invalid token format"));

        jwtAuthenticationFilterWeb.doFilterInternal(request, response, chain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response, times(1)).getWriter(); // Verifica que se haya llamado a getWriter
        verify(chain, never()).doFilter(request, response);
    }



    
}