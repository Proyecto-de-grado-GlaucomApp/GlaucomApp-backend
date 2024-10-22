
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.auth.service.AuthService;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private MyUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    public AuthServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    

    @Override
    public MyUser register(MyUser user) {
        //Review if fields are not empty or null
        if (user.getUsername() == null || user.getUsername().isEmpty() || user.getPassword() == null
                || user.getPassword().isEmpty() || user.getName() == null || user.getName().isEmpty()){
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
    public void logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0); 
        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);
    }
}
