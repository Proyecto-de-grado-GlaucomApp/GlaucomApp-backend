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
    void testValidateJwtId_ValidJwtMatchingUserId() throws Throwable {
        String token = "validToken";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractSubject(token)).thenReturn(String.valueOf(userId));
        when(jwtUtil.extractRole(token)).thenReturn("USER");

        jwtIdValidationAspect.validateJwtId(joinPoint, null, userId);

        // Verify that the original method is called
        // No exception should be thrown
    }

    @Test
    void testValidateJwtId_ValidJwtNonMatchingUserId() throws Throwable {
        String token = "validToken";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractSubject(token)).thenReturn("2");
        when(jwtUtil.extractRole(token)).thenReturn("USER");

        assertThrows(UnauthorizedException.class, () -> {
            jwtIdValidationAspect.validateJwtId(joinPoint, null, userId);
        });
    }

    @Test
    void testValidateJwtId_ValidJwtAdminRole() throws Throwable {
        String token = "validToken";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractSubject(token)).thenReturn("2");
        when(jwtUtil.extractRole(token)).thenReturn("ADMIN");

        jwtIdValidationAspect.validateJwtId(joinPoint, null, userId);

        // Verify that the original method is called
        // No exception should be thrown
    }

    @Test
    void testValidateJwtId_InvalidJwt() throws Throwable {
        when(request.getHeader("Authorization")).thenReturn(null); // Simulate no Authorization header
    
        assertThrows(UnauthorizedException.class, () -> {
            jwtIdValidationAspect.validateJwtId(joinPoint, null, 1L);
        });
    }
    
}