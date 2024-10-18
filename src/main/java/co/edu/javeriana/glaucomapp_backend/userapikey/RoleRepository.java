package co.edu.javeriana.glaucomapp_backend.userapikey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRole(RoleEnum role);
}