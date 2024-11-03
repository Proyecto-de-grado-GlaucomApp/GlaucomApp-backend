
/**
 * Service class that implements the UserDetailsService interface to provide 
 * user details for authentication and authorization.
 * 
 * This class is annotated with @Service to indicate that it is a Spring service 
 * component. It uses a MyUserRepository to fetch user details from the database.
 * 
 * The loadUserByUsername method is annotated with @Transactional to ensure that 
 * the database operations are executed within a transaction.
 * 
 * Methods:
 * - loadUserByUsername(String username): Loads the user details by username. 
 *   Throws UsernameNotFoundException if the user is not found.
 * - getRole(MyUser user): Returns the role of the user. If the role is null, 
 *   it defaults to "MOBILE".
 * 
 * Dependencies:
 * - MyUserRepository: Repository to fetch user details from the database.
 * 
 * Annotations:
 * - @Service: Indicates that this class is a Spring service component.
 * - @Autowired: Injects the MyUserRepository dependency.
 * - @Transactional: Ensures that the loadUserByUsername method is executed 
 *   within a transaction.
 * 
 * Exceptions:
 * - UsernameNotFoundException: Thrown if the user is not found in the database.
 */
package co.edu.javeriana.glaucomapp_backend.mobileauth.exposed;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.mobileauth.repository.MyUserRepository;
import jakarta.transaction.Transactional;



@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private MyUserRepository userRepository;

    /**
     * Loads the user details by the given username.
     *
     * @param username the username identifying the user whose data is required.
     * @return UserDetails containing the user's information.
     * @throws UsernameNotFoundException if the user with the given username is not found.
     */
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<MyUser> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            var userObj = user.get();
            return User.builder()
                    .username(userObj.getUsername())
                    .password(userObj.getPassword())
                    .roles(getRole(userObj))
                    .build();
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

    /**
     * Retrieves the role of the specified user. If the user does not have a role assigned,
     * it defaults to "MOBILE".
     *
     * @param user the user whose role is to be retrieved
     * @return the role of the user, or "MOBILE" if the user has no role assigned
     */
    private String getRole(MyUser user) {
        if (user.getRole() == null) {
            return "MOBILE";
        }
        return user.getRole();
    }
    
}
