package co.edu.javeriana.glaucomapp_backend.clinical_history.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;


@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID>{

    List<Exam> findByPacient_OphthalUser_IdAndPacient_Id(UUID ophtalId, UUID pacientId);


}
