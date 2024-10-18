/**
 * AuthService interface provides methods for user authentication operations.
 * It includes methods for user registration, login, and logout.
 * 
 * Methods:
 * - {@link #register(MyUser)}: Registers a new user.
 * - {@link #login(LogInForm, HttpServletResponse)}: Logs in a user with the provided login form and sets the response.
 * - {@link #logout(HttpServletResponse)}: Logs out the current user and sets the response.
 */
package co.edu.javeriana.glaucomapp_backend.auth.service;

import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.auth.model.MyUser;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface AuthService {
    public MyUser register(MyUser user);
    public void login(LogInForm loginForm, HttpServletResponse response);
    public void logout(HttpServletResponse response);
}
