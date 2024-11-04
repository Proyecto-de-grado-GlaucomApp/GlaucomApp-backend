package co.edu.javeriana.glaucomapp_backend.apikeymanagement;

import org.aspectj.lang.ProceedingJoinPoint;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;

public class JwtIdValidationAspectTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private JwtIdValidationAspect jwtIdValidationAspect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void testValidateJwtId() throws Throwable {
        String token = "validToken";
        Long validUserId = 1L;
        Long nonMatchingUserId = 2L;

        // Valid JWT matching user ID
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractSubject(token)).thenReturn(String.valueOf(validUserId));
        when(jwtUtil.extractRole(token)).thenReturn("USER");
        
        jwtIdValidationAspect.validateJwtId(joinPoint, null, validUserId); // No exception expected

        // Valid JWT non-matching user ID
        when(jwtUtil.extractSubject(token)).thenReturn(String.valueOf(nonMatchingUserId));
        
        assertThrows(UnauthorizedException.class, () -> {
            jwtIdValidationAspect.validateJwtId(joinPoint, null, validUserId);
        });

        // Valid JWT with ADMIN role
        when(jwtUtil.extractRole(token)).thenReturn("ADMIN");
        
        jwtIdValidationAspect.validateJwtId(joinPoint, null, validUserId); // No exception expected

        // Invalid JWT
        when(request.getHeader("Authorization")).thenReturn(null); // Simulate no Authorization header
        
        assertThrows(UnauthorizedException.class, () -> {
            jwtIdValidationAspect.validateJwtId(joinPoint, null, validUserId);
        });
    }
}
