package co.edu.javeriana.glaucomapp_backend.userapikey;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserApiRepository extends JpaRepository<UserApi, Long> {
    Optional<UserApi> findByEmail(String email);

}