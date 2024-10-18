/**
 * JwtAuthenticationFilter is a filter that intercepts HTTP requests to validate JWT tokens.
 * It extends OncePerRequestFilter to ensure that the filter is executed once per request.
 * 
 * This filter checks the Authorization header for a Bearer token, extracts the JWT, and validates it.
 * If the token is valid, it sets the authentication in the SecurityContextHolder.
 * 
 * Dependencies:
 * - JwtService: Service to handle JWT operations such as extracting username and validating tokens.
 * - MyUserDetailService: Service to load user details by username.
 * 
 * Methods:
 * - doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain):
 *   This method is overridden to implement the filtering logic. It checks for the Authorization header,
 *   extracts and validates the JWT, and sets the authentication in the security context if the token is valid.
 * 
 * Annotations:
 * - @Configuration: Indicates that this class is a configuration class.
 * - @Autowired: Marks the fields to be injected with their respective beans.
 * 
 * @throws ServletException if an error occurs during the filtering process.
 * @throws IOException if an I/O error occurs during the filtering process.
 */
package co.edu.javeriana.glaucomapp_backend.security.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;


import co.edu.javeriana.glaucomapp_backend.auth.service.MyUserDetailService;
import co.edu.javeriana.glaucomapp_backend.common.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyUserDetailService myUserDetailService;

      @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = myUserDetailService.loadUserByUsername(username);
            if (userDetails != null && jwtService.isTokenValid(jwt)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username,
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
