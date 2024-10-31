/**
 * AuthController is a REST controller that handles authentication-related requests.
 * It provides endpoints for user registration, login, and logout.
 * 
 * Endpoints:
 * - POST /auth/register: Registers a new user.
 * - POST /auth/login: Authenticates a user and sets a token.
 * - POST /auth/logout: Logs out a user.
 * 
 * Dependencies:
 * - AuthService: Service class that handles the business logic for authentication.
 * 
 * Methods:
 * - createUser(MyUser user): Registers a new user. Returns a ResponseEntity with a success or error message.
 * - authenticateAndSetToken(LogInForm loginForm, HttpServletResponse response): Authenticates a user and sets a token. Returns a ResponseEntity with a success or error message.
 * - logout(HttpServletResponse response): Logs out a user. Returns a ResponseEntity with a success or error message.
 * 
 * Exception Handling:
 * - Handles exceptions and returns appropriate HTTP status codes and messages.
 *   - 409 Conflict: Username already in use.
 *   - 400 Bad Request: Empty fields are not allowed.
 *   - 500 Internal Server Error: Other registration failures.
 *   - 401 Unauthorized: Authentication or logout failures.
 * 
 * Annotations:
 * - @RestController: Indicates that this class is a REST controller.
 * - @RequestMapping("/auth"): Maps requests to /auth to this controller.
 * - @PostMapping: Maps POST requests to the respective methods.
 * - @Autowired: Injects the AuthService dependency.
 * 
 * @see AuthService
 * @see MyUser
 * @see LogInForm
 */
package co.edu.javeriana.glaucomapp_backend.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.auth.model.RefreshTokenRequest;
import co.edu.javeriana.glaucomapp_backend.auth.service.AuthService;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("mobile/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> createUser(@RequestBody MyUser user) {
        try {
            authService.register(user);
            return ResponseEntity.ok("User Created Successfully");
        } catch (Exception e) {
            if (e.getMessage().contains("Username already in use")) {
                return ResponseEntity.status(409).body("Register failed: " + e.getMessage());
            }
            if (e.getMessage().contains("Empty fields are not allowed")) {
                return ResponseEntity.badRequest().body("Register failed: " + e.getMessage());
            }
            return ResponseEntity.internalServerError().body("Register failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateAndSetToken(@RequestBody LogInForm loginForm,
            HttpServletResponse response) {
        try {
            authService.login(loginForm, response);
            return ResponseEntity.ok("Authentication successful");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestBody RefreshTokenRequest token, HttpServletResponse response) {
        try {
            authService.refreshToken(token.refreshToken(), response);
            return ResponseEntity.ok("Token refreshed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token refresh failed: " + e.getMessage());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body("Token refresh failed: " + e.getMessage());
        }catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body("Token refresh failed: " + e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Token refresh failed, try later"+ e.getMessage());
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        try {
            authService.logout(token, response);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Logout failed: " + e.getMessage());
        }
    }

    //closeAccount
    @PostMapping("/closeaccount")
    public ResponseEntity<String> closeAccount(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        try {
            authService.closeAccount(token, response);
            return ResponseEntity.ok("Account closed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Account close failed: " + e.getMessage());
        }
    }

}