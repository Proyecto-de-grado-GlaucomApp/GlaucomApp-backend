@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"apikeymanagement::exposed",
                           "common",
                        "mobileauth::exposed"}
)
/**
 * Security module configuration for the GlaucomApp backend.
 * <p>
 * This module is responsible for managing security concerns within the application,
 * including authentication, authorization, and other security features.
 * It allows dependencies only from the API Key module's exposed interface.
 * </p>
 */
@org.springframework.modulith.NamedInterface("exposed")
package co.edu.javeriana.glaucomapp_backend.security;
