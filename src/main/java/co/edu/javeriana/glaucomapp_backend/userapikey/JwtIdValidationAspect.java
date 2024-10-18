package co.edu.javeriana.glaucomapp_backend.userapikey;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;


@Aspect
@Component
@AllArgsConstructor
public class JwtIdValidationAspect {

    
    private final JwtUtil jwtUtil;

    @Around("@annotation(validateJwtId) && args(userId,..)") // Captura el ID del método
    public Object validateJwtId(ProceedingJoinPoint joinPoint, ValidateJwtId validateJwtId, Long userId) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String token = extractJwtFromRequest(request);
        
        // Check if the token is null
        if (token == null) {
            throw new UnauthorizedException("Unauthorized: No token provided");
        }
        
        String jwtId = jwtUtil.extractSubject(token);
        
        // Check if the role extraction returns null
        String role = jwtUtil.extractRole(token);
        if (role != null && role.equals("ADMIN")) {
            return joinPoint.proceed();
        }
        
        // Compares the ID from the path variable with the JWT subject
        if (jwtId == null || !jwtId.equals(String.valueOf(userId))) {
            System.out.println("JWT ID: " + jwtId + " Path Variable: " + userId);
            throw new UnauthorizedException("Unauthorized");
        }
        
        return joinPoint.proceed(); // Proceed to the original method
    }
    
    
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Obtiene el encabezado Authorization del request
        String bearerToken = request.getHeader("Authorization");
    
        // Verifica si el encabezado Authorization está presente y comienza con "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Extrae el JWT eliminando el prefijo "Bearer "
            return bearerToken.substring(7);
        }
    
        // Devuelve null si el encabezado Authorization no está presente o no comienza con "Bearer "
        return null;
    }
}
