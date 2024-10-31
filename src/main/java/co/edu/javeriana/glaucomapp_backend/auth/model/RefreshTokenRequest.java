/**
 * A request object for refreshing an authentication token.
 * This record encapsulates the refresh token string.
 *
 * @param refreshToken the refresh token used to obtain a new access token
 */
package co.edu.javeriana.glaucomapp_backend.auth.model;


public record RefreshTokenRequest (String refreshToken){

}

