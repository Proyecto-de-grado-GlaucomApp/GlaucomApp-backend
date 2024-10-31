
package co.edu.javeriana.glaucomapp_backend.auth.service.impl;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.auth.model.LogInForm;
import co.edu.javeriana.glaucomapp_backend.auth.repository.DoctorEventService;
import co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository;
import co.edu.javeriana.glaucomapp_backend.auth.service.AuthService;
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


    @Override
    public MyUser register(MyUser user) {
        validateUserFields(user);
        checkUsernameAvailability(user.getUsername());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public void login(LogInForm loginForm, HttpServletResponse response) {
        Authentication authentication = authenticateUser(loginForm);
        MyUser user = findUserByUsername(((UserDetails) authentication.getPrincipal()).getUsername());

        String token = jwtUtil.generateToken(user);
        setJwtCookie(response, token);
    }

    @Override
    public void refreshToken(String token, HttpServletResponse response) {
        String newToken = jwtUtil.refreshToken(token);
        invalidateJwtCookie(response);
        setJwtCookie(response, newToken);
    }

    @Override
    public void logout(String authHeader, HttpServletResponse response) {
        validateAuthToken(authHeader);
        jwtUtil.invalidateToken(authHeader);
        invalidateJwtCookie(response);
    }

    @Override
    public void closeAccount(String token, HttpServletResponse response) {
        validateAuthToken(token);
        UUID id = UUID.fromString(jwtUtil.extractIdFromToken(token));
        logout(token, response);

        doctorEventService.deletePatient(id);
        userRepository.deleteById(id);
    }



    private void validateUserFields(MyUser user) {
        if (isNullOrEmpty(user.getUsername()) || isNullOrEmpty(user.getPassword()) || isNullOrEmpty(user.getName())) {
            throw new IllegalArgumentException("All fields are required");
        }
    }

    private void checkUsernameAvailability(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }
    }
    private Authentication authenticateUser(LogInForm loginForm) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginForm.username(), loginForm.password())
        );
    }

    private MyUser findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

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

    private void invalidateJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
    }

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

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}