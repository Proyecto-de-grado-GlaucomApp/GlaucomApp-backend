
/**
 * AuthServiceImpl is a service implementation class that handles user authentication and registration.
 * It provides methods for user registration, login, and logout.
 * 
 *  This class uses Spring's {@link Service} annotation to indicate that it's a service component.
 * It also uses dependency injection to inject required dependencies such as {@link MyUserRepository},
 * {@link PasswordEncoder}, {@link AuthenticationManager}, and {@link jwtUtil}.
 * 
 *  Methods:
 *  
 *    - {@link #register(MyUser)} - Registers a new user after validating the input and encoding the password. 
 *    - {@link #login(LogInForm, HttpServletResponse)} - Authenticates a user and generates a JWT token, which is set in an HTTP cookie. 
 *    - {@link #logout(HttpServletResponse)} - Logs out a user by invalidating the JWT token cookie. 
 *  
 * 
 *  Exceptions:
 *  
 *    - {@link IllegalArgumentException} - Thrown if the registration input fields are empty or if the username is already in use. 
 *    - {@link UsernameNotFoundException} - Thrown if the login credentials are invalid or if the user is not found. 
 *  
 * 
 *  Dependencies:
 *  
 *    - {@link MyUserRepository} - Repository for user data access. 
 *    - {@link PasswordEncoder} - Encoder for user passwords. 
 *    - {@link AuthenticationManager} - Manager for authentication processes. 
 *    - {@link jwtUtil} - Service for generating JWT tokens. 
 *  
 * 
 *  Security:
 *  
 *    - JWT tokens are set in HTTP-only, secure cookies with a SameSite attribute to prevent CSRF attacks. 
 *  
 * 
 * @see co.edu.javeriana.glaucomapp_backend.auth.service.AuthService
 * @see co.edu.javeriana.glaucomapp_backend.auth.model.MyUser
 * @see co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm
 * @see co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository
 * @see co.edu.javeriana.glaucomapp_backend.auth.config.jwtUtil
 */

package co.edu.javeriana.glaucomapp_backend.auth.service.impl;

import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.impl.*;

import co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.auth.service.AuthService;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;
import co.edu.javeriana.glaucomapp_backend.auth.event.OphtalmologistDeletedEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthServiceImpl implements AuthService {

    
    private final MyUserRepository userRepository;

    
    private final PasswordEncoder passwordEncoder;

    
    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final ApplicationEventPublisher events;

    public AuthServiceImpl(JwtUtil jwtUtil, MyUserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, ApplicationEventPublisher events) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.events = events;
    }

    @Override
    public MyUser register(MyUser user) {
        // Review if fields are not empty or null
        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getPassword() == null
                || user.getPassword().isEmpty() || user.getName() == null || user.getName().isEmpty()) {
            throw new IllegalArgumentException("Empty fields are not allowed");
        }
        // Check if the username is already in use
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public void login(LogInForm loginForm, HttpServletResponse response) {
        System.out.println("Login form: " + loginForm.username() + " " + loginForm.password());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginForm.username(), loginForm.password()));
        System.out.println("Authentication: " + authentication);

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        MyUser user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user);

        System.out.println("Token: " + token);

        Cookie jwtCookie = new Cookie("jwtToken", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(-1);
        // jwtCookie.setSameSite("Strict");
        // Manually set the SameSite attribute
        response.setHeader("Set-Cookie", String.format("%s=%s; HttpOnly; Secure; SameSite=Strict; Max-Age=%d; Path=/",
                jwtCookie.getName(), jwtCookie.getValue(), jwtCookie.getMaxAge()));

        response.addCookie(jwtCookie);
    }

    @Override
    public void refreshToken(String token, HttpServletResponse response) {
        // Generate the new token
        String newToken = jwtUtil.refreshToken(token);

        // Delete the expired cookie by setting its max age to 0
        Cookie expiredCookie = new Cookie("jwtToken", null);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(true);
        expiredCookie.setMaxAge(0); // This will tell the browser to delete the cookie
        response.addCookie(expiredCookie);

        // Set the new token in a cookie
        Cookie jwtCookie = new Cookie("jwtToken", newToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(-1); // Session cookie

        // Add SameSite attribute to the Set-Cookie header
        response.setHeader("Set-Cookie", String.format("%s=%s; HttpOnly; Secure; SameSite=Strict; Path=/",
                jwtCookie.getName(), jwtCookie.getValue()));

        // Add the new cookie to the response
        response.addCookie(jwtCookie);
    }

    @Override
    public void logout(String authHeader, HttpServletResponse response) {
        System.out.println("We are on log out: " + authHeader);
        if (jwtUtil.extractIdFromToken(authHeader) == null) {
            throw new UnauthorizedException("Invalid Token or ophtalmologist ID not found.");
        }
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid API Key");
        }
        if (jwtUtil.isTokenExpired(authHeader.substring(7).trim())) {
            // Token is expired error message
            throw new UnauthorizedException("Token is expired");
        }
        System.out.println("after token expired");
        if (!jwtUtil.validateToken(authHeader.substring(7).trim())) {
            throw new UnauthorizedException("Invalid Token");
        }
        System.out.println("after validate token");
        // Invalidate the token
        jwtUtil.invalidateToken(authHeader);

        // Invalidate cookie
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);
    }

    @Override
    public void closeAccount(String token, HttpServletResponse response) {
        UUID id = UUID.fromString(jwtUtil.extractIdFromToken(token));
        logout(token, response);

        events.publishEvent(new OphtalmologistDeletedEvent(id));
        // Review if the ophtal has patients and if patients have exams
        
        // Delete the user from the database
        System.out.println("Token on Close accoun method: " + token);
        userRepository.deleteById(id);
    }

}
