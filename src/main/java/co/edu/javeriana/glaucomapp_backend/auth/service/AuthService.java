/**
 * AuthService interface provides methods for user authentication and account management.
 * It includes methods for user registration, login, logout, token refresh, and account closure.
 */
package co.edu.javeriana.glaucomapp_backend.auth.service;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface AuthService {
    public MyUser register(MyUser user);
    public void login(LogInForm loginForm, HttpServletResponse response);
    public void logout(String authHeader,HttpServletResponse response);
    public void refreshToken(String expiredString, HttpServletResponse response);
    public void closeAccount(String token, HttpServletResponse response);
}
