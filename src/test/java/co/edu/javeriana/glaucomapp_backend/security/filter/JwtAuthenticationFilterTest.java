package co.edu.javeriana.glaucomapp_backend.security.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MyUserDetailService myUserDetailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    private final String validToken = "Bearer valid.jwt.token";
    private final String username = "testUser";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/mobile/glaucoma-screening/test");
        when(request.getHeader("Authorization")).thenReturn(null);
    
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    
        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
        verify(filterChain, never()).doFilter(request, response);
    }
    
    @Test
    void testDoFilterInternal_RequestNotMatched_ShouldProceedWithoutAuthentication() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/some/other/uri");
        when(request.getHeader("Authorization")).thenReturn(validToken);
    
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    
        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
        verify(securityContext, never()).setAuthentication(any());
    }
}    
