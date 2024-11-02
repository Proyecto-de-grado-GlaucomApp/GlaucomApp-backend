package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.javeriana.glaucomapp_backend.common.exceptions.EmailAlreadyExistsException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserApiServiceImpl {

    private final UserApiRepository userApiRepository;

    
    private final RoleRepository roleRepository;

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    public boolean registerUser(UserApiDTO userApi) {

        if (userApiRepository.findByEmail(userApi.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        UserApi user = new UserApi();

        user.setEmail(userApi.email());
        user.setContactName(userApi.contactName());
        user.setEntity(userApi.entity());
        user.setHashedPassword(passwordEncoder().encode(userApi.plainPassword()));
        user.setRole(roleRepository.findByRole(RoleEnum.USER));
        

        UserApi savedUser = userApiRepository.save(user);
    
   
        return savedUser != null;
    }

    public boolean verifyPassword(UserApi userApi, String password) {
        return passwordEncoder().matches(password, userApi.getHashedPassword());
    }
    
    public Optional<UserApi> findUserByEmail(String email) {
        return userApiRepository.findByEmail(email);
    }



    UserApi editUser(Long userId, UserApiDTO userApi) {
        Optional<UserApi> userApiOptional = userApiRepository.findByEmail(userApi.email());
        if (userApiOptional.isPresent() && !userApiOptional.get().getId().equals(userId)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        UserApi user = userApiRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(userApi.email());
        user.setContactName(userApi.contactName());
        user.setEntity(userApi.entity());

        user.setHashedPassword(passwordEncoder().encode(userApi.plainPassword()));
        return userApiRepository.save(user);
    }



}