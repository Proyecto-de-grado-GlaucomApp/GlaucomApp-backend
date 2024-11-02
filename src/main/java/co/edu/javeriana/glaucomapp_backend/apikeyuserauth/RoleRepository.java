package co.edu.javeriana.glaucomapp_backend.apikeyuserauth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRole(RoleEnum role);
}