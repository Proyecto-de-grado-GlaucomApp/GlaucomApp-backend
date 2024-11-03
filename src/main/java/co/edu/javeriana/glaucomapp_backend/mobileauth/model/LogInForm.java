/**
 * A record representing a login form with a username and password.
 * 
 * @param username the username of the user attempting to log in
 * @param password the password of the user attempting to log in
 */

package co.edu.javeriana.glaucomapp_backend.mobileauth.model;


public record LogInForm (String username, String password) {
}