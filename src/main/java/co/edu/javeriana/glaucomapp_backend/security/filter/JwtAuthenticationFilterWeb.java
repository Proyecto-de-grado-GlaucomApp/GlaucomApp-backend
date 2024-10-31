package co.edu.javeriana.glaucomapp_backend.security.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter for validating JWT tokens and setting the security context.
 */
@Component
public class JwtAuthenticationFilterWeb extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilterWeb(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * This method processes the incoming request, validates the JWT token,
     * and sets the security context if the token is valid.
     *
     * @param request  The HttpServletRequest
     * @param response The HttpServletResponse
     * @param chain    The FilterChain to continue processing
     * @throws ServletException If a servlet exception occurs
     * @throws IOException      If an I/O exception occurs
     */
    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
    
        String username = null;
        String jwt = null;
    
        // Check if there is an authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract the token
    
            try {
                username = jwtUtil.extractEmail(jwt); // Extract the username from the JWT                
            } catch (ExpiredJwtException e) {
                // Handle expired JWT exceptions
                setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired", response);
                return; // Stop the filter chain
            } catch (UnsupportedJwtException e) {
                // Handle unsupported JWT exceptions
                setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported token", response);
                return; // Stop the filter chain
            } catch (Exception e) {
                // Handle other exceptions for extracting the username
                setErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Invalid token format", response);
                return;
            }
        }
    
        // If the username is present and no previous authentication is set in the security context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Validate the JWT token
                if (jwtUtil.validateToken(jwt)) {
                    String role = jwtUtil.extractRole(jwt); // Ensure this correctly returns the role
    
                    // Create an authority based on the role
                    List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
    
                    // Set the security context
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Error response if the token is invalid
                    setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token", response);
                    return; // Stop the filter chain
                }
            } catch (IllegalArgumentException e) {
                // Handle exceptions for illegal arguments during JWT extraction
                setErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Invalid token format", response);
                return; // Stop the filter chain
            } catch (MalformedJwtException e) {
                // Handle exceptions for malformed JWTs
                setErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Malformed token", response);
                return; // Stop the filter chain
            } catch (IOException e) {
                // Handle any other exceptions during token validation
                setErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token validation error: " + e.getMessage(), response);
                return; // Stop the filter chain
            }
        }
    
        // Continue with the filter chain
        chain.doFilter(request, response);
    }
    

    /**
     * Sends a custom error response.
     *
     * @param status  The HTTP status code
     * @param message The error message
     * @param response The HttpServletResponse
     * @throws IOException If an I/O exception occurs
     */
    private void setErrorResponse(int status, String message, HttpServletResponse response) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status);
        errorDetails.put("message", message);
        errorDetails.put("timestamp", System.currentTimeMillis());
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorDetails));
    }
}
