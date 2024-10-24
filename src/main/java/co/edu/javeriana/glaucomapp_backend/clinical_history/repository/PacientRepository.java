package co.edu.javeriana.glaucomapp_backend.clinical_history.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@Repository
public interface PacientRepository extends JpaRepository<Pacient, UUID> {

    Pacient findPacientByCedulaAndOphthalUser(String cedula, MyUser ophthalUser);

    @Query("SELECT p FROM Pacient p WHERE p.ophthalUser.id = :ophtalId")
    List<Pacient> findAllPacientsByOpthalUserId(@Param("ophtalId") UUID ophtalId);

    @Modifying
    @Query("DELETE FROM Pacient p WHERE p.ophthalUser.id = :ophtalId AND p.id = :pacientId")
    void deletePacientByOphtalID(@Param("ophtalId") UUID ophtalId, @Param("pacientId")UUID pacientId);



}
