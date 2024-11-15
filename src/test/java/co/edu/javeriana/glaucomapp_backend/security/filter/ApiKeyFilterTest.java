package co.edu.javeriana.glaucomapp_backend.security.filter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import co.edu.javeriana.glaucomapp_backend.security.apikey.ClientAuthenticationHelper;

import java.io.IOException;

class ApiKeyFilterTest {

    @Mock
    private ClientAuthenticationHelper authServiceHelper;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private FilterChain chain;

    private ApiKeyFilter apiKeyFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        apiKeyFilter = new ApiKeyFilter(authServiceHelper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // Limpia el contexto de seguridad
    }

    @Test
    void doFilter_ShouldContinueChain_WhenRequestUriIsNotGlaucomaScreening() throws IOException, ServletException {
        // Arrange
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/other-endpoint");

        // Act
        apiKeyFilter.doFilter(httpRequest, httpResponse, chain);

        // Assert
        verify(chain).doFilter(httpRequest, httpResponse); // Verifica que se llama a la cadena
        verify(authServiceHelper, never()).validateApiKey(anyString()); // No se debe validar la API key
    }

    @Test
    void doFilter_ShouldReturnUnauthorized_WhenApiKeyIsMissingOrInvalid() throws IOException, ServletException {
        // Arrange
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/glaucoma-screening");

        // Test missing API key
        when(httpRequest.getHeader("X-API-KEY")).thenReturn(null);

        // Act
        apiKeyFilter.doFilter(httpRequest, httpResponse, chain);

        // Assert
        verify(httpResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key"); // Verifica respuesta de error
        verify(chain, never()).doFilter(httpRequest, httpResponse); // No debe continuar en la cadena

        // Test invalid API key
        String invalidApiKey = "invalid-api-key";
        when(httpRequest.getHeader("X-API-KEY")).thenReturn(invalidApiKey);
        when(authServiceHelper.validateApiKey(invalidApiKey)).thenReturn(false);

        // Act
        apiKeyFilter.doFilter(httpRequest, httpResponse, chain);

        // Assert
        verify(httpResponse, times(2)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
        verify(chain, never()).doFilter(httpRequest, httpResponse); // No debe continuar en la cadena
    }

    @Test
    void doFilter_ShouldSetAuthentication_WhenApiKeyIsValid() throws IOException, ServletException {
        // Arrange
        String validApiKey = "valid-api-key";
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/glaucoma-screening");
        when(httpRequest.getHeader("X-API-KEY")).thenReturn(validApiKey);
        when(authServiceHelper.validateApiKey(validApiKey)).thenReturn(true);

        // Act
        apiKeyFilter.doFilter(httpRequest, httpResponse, chain);

        // Assert
        verify(chain).doFilter(httpRequest, httpResponse); // Verifica que se llama a la cadena
        verify(authServiceHelper).validateApiKey(validApiKey); // Verifica que se validó la API key
        assertNotNull(SecurityContextHolder.getContext().getAuthentication()); // Verifica que la autenticación está establecida
    }
}
