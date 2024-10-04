/**
 * Security configuration class for the GlaucomApp backend.
 * This class configures the security settings for the application, including authentication and authorization mechanisms.
 * 
 * Annotations:
 * - @Configuration: Indicates that this class is a configuration class.
 * - @EnableWebSecurity: Enables Spring Security's web security support.
 * 
 * Dependencies:
 * - MyUserDetailService: Custom user details service for loading user-specific data.
 * - JwtAuthenticationFilter: Custom JWT authentication filter for processing JWT tokens.
 * 
 * Beans:
 * - SecurityFilterChain: Configures the security filter chain, including CSRF protection, request authorization, form login, and JWT filter.
 * - UserDetailsService: Provides the custom user details service.
 * - AuthenticationProvider: Configures the authentication provider with a DAO-based authentication provider and password encoder.
 * - AuthenticationManager: Manages authentication with the configured authentication provider.
 * - PasswordEncoder: Provides a BCrypt password encoder for encoding passwords.
 * 
 * Security Configuration:
 * - Disables CSRF protection.
 * - Configures request authorization:
 *   - Permits all requests to "/auth/**".
 *   - Requires "MOBILE" role for requests to "/register/**" and "/login".
 *   - Requires authentication for all other requests.
 * - Permits all users to access the form login page.
 * - Adds the JWT authentication filter before the UsernamePasswordAuthenticationFilter.
 */
package co.edu.javeriana.glaucomapp_backend.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.edu.javeriana.glaucomapp_backend.auth.filter.JwtAuthenticationFilter;
import co.edu.javeriana.glaucomapp_backend.auth.service.MyUserDetailService;

import org.springframework.beans.factory.annotation.Autowired;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MyUserDetailService userDetailService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/auth/**").permitAll();
                    registry.requestMatchers("/register/**", "/login").hasRole("MOBILE");
                    registry.anyRequest().authenticated();
                })
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
