package co.edu.javeriana.glaucomapp_backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.edu.javeriana.glaucomapp_backend.auth.service.MyUserDetailService;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.security.apikey.ClientAuthenticationHelper;
import co.edu.javeriana.glaucomapp_backend.security.filter.ApiKeyFilter;
import co.edu.javeriana.glaucomapp_backend.security.filter.JwtAuthenticationFilterWeb;
import co.edu.javeriana.glaucomapp_backend.security.filter.JwtAuthenticationFilter;


/**
 * Configuration class for Spring Security.
 * <p>
 * This class configures security settings for the application, including the integration
 * of API key authentication and JWT token authentication through custom filters.
 * </p>
 */
@Configuration
public class WebSecurityConfig {

    @Autowired
    private MyUserDetailService userDetailService;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
    /**
     * Provides a PasswordEncoder bean for encoding passwords.
     * 
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

        @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public JwtAuthenticationFilterWeb jwtAuthenticationFilterWeb() {
        return new JwtAuthenticationFilterWeb(jwtUtil());
    }

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailService;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

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
     * Configures the security filter chain for API key authentication.
     * 
     * @param http the HttpSecurity to configure
     * @return a SecurityFilterChain for API key authentication
     * @throws Exception if an error occurs during configuration
     */

    @Bean
    @Order(4)
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .securityMatcher("/mobile/auth")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/register/**", "/login").permitAll();
                    registry.anyRequest().permitAll();
                })
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http) throws Exception {
        http
        .securityMatcher("/glaucoma-screening")
            .addFilterBefore(new ApiKeyFilter(authServiceHelper), AnonymousAuthenticationFilter.class)
            .authorizeHttpRequests(requests -> 
                requests
                    .requestMatchers("/glaucoma-screening/mobile/**").permitAll()
                    .requestMatchers("/glaucoma-screening/upload-image").permitAll()
                    // Allow all requests to /glaucoma-screening/mobile/**
                    .requestMatchers("/glaucoma-screening/third-party/**").authenticated() // Require authentication for /glaucoma-screening/third-party/**
                    .requestMatchers("/glaucoma-screening/path").permitAll() // Require ADMIN role for /glaucoma-screening/admin/**
                    .anyRequest().permitAll() // Require authentication for all other requests
                    )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session management
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF protection for APIs

        return http.build();
    }

        // A los que tienen auth no poner filtro
        @Bean
        @Order(2)
        public SecurityFilterChain noFilterSecurityFilterChain(HttpSecurity http) throws Exception {
            http
            .securityMatcher("/api/v1/api-key/auth")
                .authorizeHttpRequests(requests -> 
                    requests
                        .requestMatchers("/api/v1/api-key/auth/register").permitAll() // Allow access to registration without authentication
                        .requestMatchers("/api/v1/api-key/auth/login").permitAll() // Allow access to login without authentication
                        .anyRequest().permitAll() // Require authentication for all other requests
                )
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session management
                )
                .csrf(csrf -> csrf.disable()); // Disable CSRF protection for APIs
    
            return http.build();
        }
        
    /**
     * Configures the security filter chain for JWT token authentication.
     * 
     * @param http the HttpSecurity to configure
     * @return a SecurityFilterChain for JWT authentication
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    @Order(1)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable()) // Desactivar CSRF si usas JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .securityMatcher("/api/v1/api-keys/**")// Esta es la base de los endpoints

            .authorizeHttpRequests(authorize -> 
                authorize
                    // Asegúrate de que los ADMIN solo puedan acceder a los endpoints específicos
                    .requestMatchers("/api/v1/api-keys/{apiKeyId}/approve").hasAuthority("ADMIN") 
                    .requestMatchers("/api/v1/api-keys/approved").hasAuthority("ADMIN") 
                    .requestMatchers("/api/v1/api-keys/pending").hasAuthority("ADMIN") 
                    // Otras rutas pueden requerir otros permisos o ser accesibles por usuarios autenticados
                    .requestMatchers("/api/v1/api-keys/users/**").hasAuthority("USER")
                    .anyRequest().permitAll() // Cualquier otra petición debe estar autenticada
            )
                    .addFilterBefore(jwtAuthenticationFilterWeb(), UsernamePasswordAuthenticationFilter.class); // Añadir el filtro JWT

 
        return http.build();
    }
    


    
}
