/**
 * Repository interface for managing Pacient entities.
 * Extends JpaRepository to provide CRUD operations.
 * 
 * This repository includes custom query methods to find and delete Pacient entities
 * based on specific criteria.
 * 
 * Methods:
 * - findPacientByCedulaAndOphthalUser: Finds a Pacient by their cedula and associated ophthalmologist user.
 * - findAllPacientsByOpthalUserId: Retrieves all Pacient entities associated with a specific ophthalmologist user ID.
 * - deletePacientByOphtalID: Deletes a Pacient entity based on the ophthalmologist user ID and Pacient ID.
 * 
 * Annotations:
 * - @Repository: Indicates that this interface is a Spring Data repository.
 * - @Transactional: Ensures that methods are executed within a transactional context.
 * - @Modifying: Indicates that a query method modifies the database.
 * - @Query: Defines custom JPQL queries.
 * - @Param: Binds method parameters to query parameters.
 * 
 * Dependencies:
 * - JpaRepository: Provides CRUD operations.
 * - MyUser: Represents the authenticated user.
 * - Pacient: Represents the Pacient entity.
 * - UUID: Represents unique identifiers for entities.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@Repository
public interface PacientRepository extends JpaRepository<Pacient, UUID> {

    Pacient findPacientByCedulaAndDoctorId(String cedula, UUID doctorId);

    @Query("SELECT p FROM Pacient p WHERE p.doctorId = :doctorId")
    List<Pacient> findAllPacientsByDoctorId(@Param("doctorId") UUID doctorId);

    @Modifying
    @Query("DELETE FROM Pacient p WHERE p.doctorId = :doctorId AND p.id = :pacientId")
    void deletePacientByDoctorId(@Param("doctorId") UUID doctorId, @Param("pacientId") UUID pacientId);

}