package co.edu.javeriana.glaucomapp_backend.security.filter;

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
    private SecurityContext securityContext; // Añadir el mock del SecurityContext

    private final String validToken = "Bearer valid.jwt.token";
    private final String username = "testUser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configurar el contexto de seguridad para usar el mock
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDoFilterInternal_ValidToken_ShouldSetAuthentication() throws Exception {
        // Mock the request URI
        when(request.getRequestURI()).thenReturn("/mobile/glaucoma-screening/test");
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtUtil.extractSubject("valid.jwt.token")).thenReturn(username);
        when(myUserDetailService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("valid.jwt.token")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(filterChain).doFilter(request, response);
        verify(myUserDetailService).loadUserByUsername(username);
        verify(jwtUtil).isTokenValid("valid.jwt.token");
        verify(securityContext).setAuthentication(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(username, captor.getValue().getName());
    }


    @Test
    void testDoFilterInternal_NoAuthorizationHeader_ShouldReturnUnauthorized() throws Exception {
        when(request.getRequestURI()).thenReturn("/mobile/glaucoma-screening/test");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_RequestNotMatched_ShouldProceedWithoutAuthentication() throws Exception {
        when(request.getRequestURI()).thenReturn("/some/other/uri");
        when(request.getHeader("Authorization")).thenReturn(validToken);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
        verify(securityContext, never()).setAuthentication(any());
    }

}