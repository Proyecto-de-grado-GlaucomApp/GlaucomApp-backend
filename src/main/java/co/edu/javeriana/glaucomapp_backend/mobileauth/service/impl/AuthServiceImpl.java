
/**
 * Service implementation for authentication-related operations.
 * This class provides methods for user registration, login, token refresh, logout, and account closure.
 * It uses Spring Security for authentication and JWT for token management.
 * 
 * Dependencies:
 * - MyUserRepository: Repository for user data.
 * - AuthenticationManager: Manages authentication.
 * - JwtUtil: Utility class for JWT operations.
 * - DoctorEventService: Service for handling doctor events.
 * - PasswordEncoder: Encoder for password encryption.
 * 
 * Methods:
 * - register(MyUser user): Registers a new user after validating fields and checking username availability.
 * - login(LogInForm loginForm, HttpServletResponse response): Authenticates a user and sets a JWT cookie in the response.
 * - refreshToken(String token, HttpServletResponse response): Refreshes a JWT token and sets a new cookie in the response.
 * - logout(String authHeader, HttpServletResponse response): Invalidates the JWT token and removes the cookie.
 * - closeAccount(String token, HttpServletResponse response): Closes a user account by deleting user data and invalidating the token.
 * 
 * Private Methods:
 * - validateUserFields(MyUser user): Validates that all required user fields are present.
 * - checkUsernameAvailability(String username): Checks if the username is already in use.
 * - authenticateUser(LogInForm loginForm): Authenticates the user using the provided login form.
 * - findUserByUsername(String username): Finds a user by their username.
 * - setJwtCookie(HttpServletResponse response, String token): Sets a JWT cookie in the response.
 * - invalidateJwtCookie(HttpServletResponse response): Invalidates the JWT cookie.
 * - validateAuthToken(String authHeader): Validates the authentication token.
 * - isNullOrEmpty(String str): Checks if a string is null or empty.
 */
package co.edu.javeriana.glaucomapp_backend.mobileauth.service.impl;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.mobileauth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.mobileauth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.mobileauth.service.AuthService;
import co.edu.javeriana.glaucomapp_backend.mobileauth.service.DoctorEventService;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final MyUserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final DoctorEventService doctorEventService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    /**
     * Registers a new user in the system.
     *
     * This method performs the following steps:
     * 1. Validates the fields of the provided user object.
     * 2. Checks if the username is available.
     * 3. Encodes the user's password.
     * 4. Saves the user to the repository.
     *
     * @param user The user object containing the registration details.
     * @return The registered user object.
     */
    @Override
    public MyUser register(MyUser user) {
        validateUserFields(user);
        checkUsernameAvailability(user.getUsername());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Authenticates the user based on the provided login form, generates a JWT token,
     * and sets it as a cookie in the HTTP response.
     *
     * @param loginForm the form containing the user's login credentials
     * @param response the HTTP response where the JWT token will be set as a cookie
     */
    @Override
    public void login(LogInForm loginForm, HttpServletResponse response) {
        Authentication authentication = authenticateUser(loginForm);
        MyUser user = findUserByUsername(((UserDetails) authentication.getPrincipal()).getUsername());

        String token = jwtUtil.generateToken(user);
        setJwtCookie(response, token);
    }

    /**
     * Refreshes the given JWT token and updates the response with a new token.
     * 
     * @param token the JWT token to be refreshed
     * @param response the HttpServletResponse to which the new token will be added
     */
    @Override
    public void refreshToken(String token, HttpServletResponse response) {
        String newToken = jwtUtil.refreshToken(token);
        invalidateJwtCookie(response);
        setJwtCookie(response, newToken);
    }

    /**
     * Logs out the user by invalidating the JWT token and removing the JWT cookie.
     *
     * @param authHeader the authorization header containing the JWT token
     * @param response the HTTP response to modify for removing the JWT cookie
     */
    @Override
    public void logout(String authHeader, HttpServletResponse response) {
        validateAuthToken(authHeader);
        jwtUtil.invalidateToken(authHeader);
        invalidateJwtCookie(response);
    }

    /**
     * Closes the account associated with the provided token.
     * 
     * This method performs the following actions:
     * 1. Validates the provided authentication token.
     * 2. Extracts the user ID from the token.
     * 3. Logs out the user.
     * 4. Deletes the patient's data from the doctor event service.
     * 5. Deletes the user from the repository.
     * 
     * @param token the authentication token of the user whose account is to be closed
     * @param response the HTTP response object
     */
    @Override
    public void closeAccount(String token, HttpServletResponse response) {
        validateAuthToken(token);
        UUID id = UUID.fromString(jwtUtil.extractIdFromToken(token));
        logout(token, response);

        doctorEventService.deletePatient(id);
        userRepository.deleteById(id);
    }



    /**
     * Validates the fields of the given user.
     * Checks if the username, password, or name fields are null or empty.
     * If any of these fields are null or empty, an IllegalArgumentException is thrown.
     *
     * @param user the user whose fields are to be validated
     * @throws IllegalArgumentException if any of the fields (username, password, name) are null or empty
     */
    private void validateUserFields(MyUser user) {
        if (isNullOrEmpty(user.getUsername()) || isNullOrEmpty(user.getPassword()) || isNullOrEmpty(user.getName())) {
            throw new IllegalArgumentException("All fields are required");
        }
    }

    /**
     * Checks if the given username is available.
     * 
     * This method queries the user repository to determine if the specified 
     * username is already in use. If the username is found in the repository, 
     * an IllegalArgumentException is thrown.
     *
     * @param username the username to check for availability
     * @throws IllegalArgumentException if the username is already in use
     */
    private void checkUsernameAvailability(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }
    }

    /**
     * Authenticates a user based on the provided login form.
     *
     * @param loginForm the form containing the user's login credentials
     * @return an Authentication object if the authentication is successful
     */
    private Authentication authenticateUser(LogInForm loginForm) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginForm.username(), loginForm.password())
        );
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username of the user to be found
     * @return the user with the specified username
     * @throws UsernameNotFoundException if no user is found with the specified username
     */
    private MyUser findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Sets a JWT token as an HTTP-only, secure cookie in the response.
     *
     * @param response the HttpServletResponse to which the cookie will be added
     * @param token the JWT token to be set in the cookie
     */
    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie("jwtToken", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(-1);
        response.setHeader("Set-Cookie", String.format("%s=%s; HttpOnly; Secure; SameSite=Strict; Path=/",
                jwtCookie.getName(), jwtCookie.getValue()));
        response.addCookie(jwtCookie);
    }

    /**
     * Invalidates the JWT cookie by setting its value to null and its max age to 0.
     * This effectively removes the cookie from the client's browser.
     *
     * @param response the HttpServletResponse to which the cookie will be added
     */
    private void invalidateJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
    }

    /**
     * Validates the provided authentication token.
     *
     * @param authHeader the authorization header containing the token
     * @throws IllegalArgumentException if the authorization header is null, empty, or does not start with "Bearer "
     * @throws UnauthorizedException if the token is invalid, expired, or the user ID cannot be extracted from the token
     */
    private void validateAuthToken(String authHeader) {
        if (isNullOrEmpty(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid API Key");
        }
        String token = authHeader.substring(7).trim();
        if (jwtUtil.extractIdFromToken(authHeader) == null) {
            throw new UnauthorizedException("Invalid Token or User ID not found.");
        }
        if (jwtUtil.isTokenExpired(token) || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Token is invalid or expired");
        }
    }

    /**
     * Checks if a given string is null or empty.
     *
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}