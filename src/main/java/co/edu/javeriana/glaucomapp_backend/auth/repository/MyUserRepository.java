/**
 * Repository interface for managing {@link MyUser} entities.
 * Extends {@link JpaRepository} to provide CRUD operations.
 * 
 * @see JpaRepository
 * @see MyUser
 */
package co.edu.javeriana.glaucomapp_backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.javeriana.glaucomapp_backend.auth.model.MyUser;

import java.util.Optional;
import java.util.UUID;

public interface MyUserRepository extends JpaRepository<MyUser, UUID> {

    Optional<MyUser> findByUsername(String username);

}