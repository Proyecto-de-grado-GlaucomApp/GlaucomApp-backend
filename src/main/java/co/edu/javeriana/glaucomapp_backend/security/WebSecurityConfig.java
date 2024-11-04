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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.mobileauth.exposed.MyUserDetailService;
import co.edu.javeriana.glaucomapp_backend.security.apikey.ClientAuthenticationHelper;
import co.edu.javeriana.glaucomapp_backend.security.filter.ApiKeyFilter;
import co.edu.javeriana.glaucomapp_backend.security.filter.JwtAuthenticationFilter;
import co.edu.javeriana.glaucomapp_backend.security.filter.JwtAuthenticationFilterWeb;


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


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil(), userDetailService);
    }



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
     @Order(6)
     public SecurityFilterChain mobileCHJWTSecurityFilterChain(HttpSecurity http) throws Exception {
         http
         .securityMatcher("/mobile/clinical_history")
         .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
             .authorizeHttpRequests(requests ->
                 requests
                     .requestMatchers("/**").authenticated() // Require ADMIN role for /glaucoma-screening/admin/**
                     .anyRequest().permitAll() // Require authentication for all other requests
                     )
             .sessionManagement(session ->
                 session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session management
             )
             .csrf(csrf -> csrf.disable()); // Disable CSRF protection for APIs
         return http.build();
     }


     @Bean
     @Order(5)
     public SecurityFilterChain mobileJWTSecurityFilterChain(HttpSecurity http) throws Exception {
         http
         .securityMatcher("/mobile/glaucoma-screening")
         .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
             .authorizeHttpRequests(requests -> 
                 requests
                     .requestMatchers("/mobile/glaucoma-screening/process").authenticated() // Require ADMIN role for /glaucoma-screening/admin/**
                     
                     .anyRequest().permitAll() // Require authentication for all other requests
                     )
                     
             .sessionManagement(session -> 
                 session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session management
             )
             .csrf(csrf -> csrf.disable()); // Disable CSRF protection for APIs
         return http.build();
     }


    @Bean
    @Order(4)
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .securityMatcher("/mobile/auth")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(registry -> {
                // Allow access to registration and login endpoints
                registry.requestMatchers("/mobile/auth/register/**", "/mobile/auth/login", "/mobile/auth/refresh").permitAll();
                // Require authentication for logout and refresh endpoints
                registry.requestMatchers("/mobile/auth/logout").authenticated();
                // For this filter, deny all other requests by default to limit its scope
                registry.anyRequest().denyAll();
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
            .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/glaucoma-screening/**") // Corregido para coincidir con el filtro
            .addFilterBefore(new ApiKeyFilter(authServiceHelper), AnonymousAuthenticationFilter.class)
            .authorizeHttpRequests(requests -> 
                requests
                    .requestMatchers("/api/v1/glaucoma-screening/**").authenticated()
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable());
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

            .securityMatcher("/api/v1/api-keys")// Esta es la base de los endpoints

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
