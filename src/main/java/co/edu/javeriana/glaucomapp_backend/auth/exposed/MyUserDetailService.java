/**
 * Service class that implements the UserDetailsService interface to provide
 * custom user authentication logic.
 * 
 * This service is responsible for loading user-specific data during the
 * authentication process.
 * 
 * @Service Indicates that this class is a Spring service component.
 */
package co.edu.javeriana.glaucomapp_backend.auth.exposed;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.auth.repository.MyUserRepository;
import jakarta.transaction.Transactional;



@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private MyUserRepository userRepository;

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

    private String getRole(MyUser user) {
        if (user.getRole() == null) {
            return "MOBILE";
        }
        return user.getRole();
    }
    
}
