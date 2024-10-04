/**
 * The AuthModule class is a configuration class for the authentication module
 * in the GlaucomApp backend application. It is annotated with @Configuration
 * to indicate that it is a Spring configuration class and with @ApplicationModule
 * to provide a display name for the module.
 * 
 * This module is responsible for handling authentication-related configurations
 * and settings within the application.
 * 
 * Annotations:
 *   - @Configuration - Marks this class as a source of bean definitions for the application context. 
 *   - @ApplicationModule - Provides a display name for the module, which is "Authentication Module". 
 *
 */
package co.edu.javeriana.glaucomapp_backend.auth;


import org.springframework.modulith.ApplicationModule;
import org.springframework.context.annotation.Configuration;


@Configuration
@ApplicationModule(displayName = "Authentication Module")
public class AuthModule {

}
