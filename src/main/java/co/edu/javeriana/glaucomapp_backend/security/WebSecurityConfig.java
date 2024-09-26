package co.edu.javeriana.glaucomapp_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Configuration class for Spring Security.
 * <p>
 * This class configures security settings for the application, including the integration
 * of API key authentication through a custom filter.
 * </p>
 */
@Configuration
public class WebSecurityConfig {
    
    private final ClientAuthenticationHelper authServiceHelper;

    /**
     * Constructs a {@link WebSecurityConfig} with the specified authentication helper.
     *
     * @param authServiceHelper the service used to validate API keys
     */
    public WebSecurityConfig(ClientAuthenticationHelper authServiceHelper) {
        this.authServiceHelper = authServiceHelper;
    }

    /**
     * Configures the {@link SecurityFilterChain} with custom settings.
     * <p>
     * Adds the {@link ApiKeyFilter} to the security chain before the 
     * {@link AnonymousAuthenticationFilter}. It defines which requests are permitted
     * without authentication and configures session management to be stateless.
     * </p>
     *
     * @param http the {@link HttpSecurity} object to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if any configuration error occurs
     */
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(new ApiKeyFilter(authServiceHelper), AnonymousAuthenticationFilter.class)
            .authorizeHttpRequests(requests -> 
                requests
                    .requestMatchers("/api-key/**").permitAll() // Allow all requests to /api-key/**
                    .requestMatchers("/glaucoma-screening/mobile").permitAll() // Allow all requests to /glaucoma-screening/**
                    .requestMatchers("/glaucoma-screening/third-party").authenticated() // Require authentication for /third-party/**
                    .anyRequest().permitAll() // Allow access without authentication to other routes
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session management
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF protection

        return http.build();
    }
}
