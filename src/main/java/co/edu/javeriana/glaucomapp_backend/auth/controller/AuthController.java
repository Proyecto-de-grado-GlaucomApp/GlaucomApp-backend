/**
 * AuthController handles authentication-related operations such as user registration,
 * login, token refresh, logout, and account closure.
 * 
 * This controller provides endpoints for:
 * 
 *   Registering a new user
 *   Authenticating a user and setting a token
 *   Refreshing an authentication token
 *   Logging out a user
 *   Closing a user account
 * 
 * 
 * Each endpoint delegates the actual processing to the {@link AuthService}.
 * 
 * @see AuthService
 */
package co.edu.javeriana.glaucomapp_backend.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.auth.model.RefreshTokenRequest;
import co.edu.javeriana.glaucomapp_backend.auth.service.AuthService;
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
        authService.register(user);
        return ResponseEntity.ok("User Created Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateAndSetToken(@RequestBody LogInForm loginForm,
            HttpServletResponse response) {
        authService.login(loginForm, response);
        return ResponseEntity.ok("Authentication successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestBody RefreshTokenRequest token, HttpServletResponse response) {
        authService.refreshToken(token.refreshToken(), response);
        return ResponseEntity.ok("Token refreshed successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        authService.logout(token, response);
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/closeaccount")
    public ResponseEntity<String> closeAccount(@RequestHeader("Authorization") String token,
            HttpServletResponse response) {
            authService.closeAccount(token, response);
            return ResponseEntity.ok("Account closed successfully");
    }

}