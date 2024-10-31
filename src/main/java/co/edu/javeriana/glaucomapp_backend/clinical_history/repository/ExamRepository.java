/**
 * Repository interface for managing {@link Exam} entities.
 * Extends {@link JpaRepository} to provide CRUD operations and custom query methods.
 * 
 * This repository is responsible for accessing and manipulating Exam data in the database.
 * It includes a custom query method to find exams by the ophthalmologist user ID and patient ID.
 * 
 * Methods:
 * 
 *   {@link #findByPacient_OphthalUser_IdAndPacient_Id(UUID, UUID)}: Finds a list of exams by the ophthalmologist user ID and patient ID.
 * 
 * 
 * @see Exam
 * @see JpaRepository
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;


@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID>{

    List<Exam> findByPacient_DoctorIdAndPacient_Id(UUID doctorId, UUID pacientId);

}
