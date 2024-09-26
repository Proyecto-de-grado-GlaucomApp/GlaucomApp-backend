package co.edu.javeriana.glaucomapp_backend.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filter for validating API keys in incoming HTTP requests.
 * <p>
 * This filter intercepts incoming requests to validate the presence and correctness of an API key.
 * If the API key is valid, it sets the authentication in the security context, allowing further processing
 * of the request. Requests to the "/third-party/" endpoint are allowed to pass without an API key check.
 * </p>
 * 
 * <p>
 * <strong>Usage:</strong> This filter should be registered in the Spring Security filter chain to ensure
 * that all relevant requests are processed for API key validation.
 * </p>
 * 
 * <p>
 * <strong>Note:</strong> The API key is expected to be included in the request header with the key "X-API-KEY".
 * </p>
 */
@RequiredArgsConstructor
public class ApiKeyFilter extends GenericFilterBean {

    private final ClientAuthenticationHelper authServiceHelper;

    /**
     * Filters incoming requests to validate API keys.
     *
     * @param request the request to filter
     * @param response the response to filter
     * @param chain the filter chain to continue processing the request
     * @throws IOException if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        /*Checks if the request is directed to the "/glaucoma-screening/third-party" endpoint.
         *If not, access is allowed without requiring an API key, 
         *and the filter chain continues to the next filter.
        */
        if (!httpRequest.getRequestURI().startsWith("/glaucoma-screening/third-party")) {
            chain.doFilter(request, response);
            return;
        }


        // Retrieve API key from request header
        String apiKey = httpRequest.getHeader("X-API-KEY");

        // Validate API key
        if (apiKey == null || !authServiceHelper.validateApiKey(apiKey)) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
            return; // Ensure this returns immediately
        }

        // API key is valid. Set authentication in the security context
        Authentication authentication = new UsernamePasswordAuthenticationToken(apiKey, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue with the next filter in the chain
        chain.doFilter(httpRequest, httpResponse);
    }
}
