/**
 * JwtAuthenticationFilter is a custom filter that extends OncePerRequestFilter to handle JWT-based authentication.
 * It intercepts incoming HTTP requests and checks for the presence of a valid JWT token in the Authorization header.
 * If the token is valid, it sets the authentication in the SecurityContextHolder.
 * 
 * The filter only applies to protected paths, specifically those starting with "/mobile/glaucoma-screening" or "/mobile/clinical_history".
 * 
 * Dependencies:
 * - JwtUtil: Utility class for handling JWT operations such as extracting the subject and validating the token.
 * - MyUserDetailService: Service for loading user details by username.
 * 
 * Constructor:
 * - JwtAuthenticationFilter(JwtUtil jwtUtil, MyUserDetailService myUserDetailService): Initializes the filter with the provided JwtUtil and MyUserDetailService instances.
 * 
 * Methods:
 * - doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain): 
 *   Main method that performs the filtering logic. It checks the Authorization header for a valid JWT token and sets the authentication context if the token is valid.
 * 
 * Usage:
 * This filter should be registered in the Spring Security filter chain to enable JWT-based authentication for the specified protected paths.
 */

package co.edu.javeriana.glaucomapp_backend.security.filter;
 
import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
@Configuration
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil;
    private final MyUserDetailService myUserDetailService;  // Changed to final
 
    public JwtAuthenticationFilter(JwtUtil jwtUtil, MyUserDetailService myUserDetailService) {  // Modified constructor
        this.jwtUtil = jwtUtil;
        this.myUserDetailService = myUserDetailService;
    }
 
      @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;


        // Check if this is a protected path
        boolean isProtectedPath = httpRequest != null && 
            (httpRequest.getRequestURI().startsWith("/mobile/glaucoma-screening") || 
            httpRequest.getRequestURI().startsWith("/mobile/clinical_history"));
             
        // If it's not a protected path, skip authentication
        if (!isProtectedPath) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // For protected paths, check authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
            return;
        }


       
 
        String jwt = authHeader.substring(7).trim();
        String username = jwtUtil.extractSubject(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = myUserDetailService.loadUserByUsername(username);
            if (userDetails != null && jwtUtil.isTokenValid(jwt)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username,
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else{
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
            }
        }
        filterChain.doFilter(request, response);
    }
}