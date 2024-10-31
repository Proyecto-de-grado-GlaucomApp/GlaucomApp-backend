/**
 * Represents a patient in the GlaucomApp system.
 * Each patient is uniquely identified by a UUID and has a unique combination of 
 * ophthalmologist ID and cedula (identification number).
 * 
 * This class is annotated with JPA annotations to map it to a database table named "pacient".
 * It uses Lombok annotations to generate boilerplate code such as getters, setters, constructors, 
 * and the builder pattern.
 * 
 * Attributes:
 * 
 *   id - The unique identifier for the patient, generated automatically.
 *   cedula - The identification number of the patient, cannot be null.
 *   name - The name of the patient, cannot be null.
 *   doctorId - The UUID of the ophthalmologist associated with the patient, cannot be null.
 *   exams - A list of exams associated with the patient, with cascade operations and orphan removal enabled.
 * 
 * 
 * Constraints:
 * 
 *   Unique constraint on the combination of ophthalmologist ID and cedula.
 * 
 * 
 * Overrides:
 * 
 *   toString - Provides a string representation of the patient object.
 * 
 * 
 * Annotations:
 * 
 *   @Entity - Specifies that this class is an entity and is mapped to a database table.
 *   @Table - Specifies the details of the table that this entity is mapped to.
 *   @Id - Specifies the primary key of the entity.
 *   @GeneratedValue - Provides the specification of generation strategies for the primary keys.
 *   @Column - Specifies the details of the column to which a field or property will be mapped.
 *   @OneToMany - Specifies a one-to-many relationship with another entity.
 *   @Data, @Builder, @AllArgsConstructor, @NoArgsConstructor - Lombok annotations to reduce boilerplate code.
 * 
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient;

import java.util.List;
import java.util.UUID;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pacient", uniqueConstraints = { @UniqueConstraint(columnNames = { "ophtal_id", "cedula" }) })
public class Pacient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "pacient_id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    private UUID id;

    @Column(name = "cedula", nullable = false)
    private String cedula;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ophtal_id", nullable = false)
    private UUID doctorId;

    @OneToMany(mappedBy = "pacient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exam> exams;

    @Override
    public String toString() {
        return "Pacient{id=" + id + ", cedula='" + cedula + "', name='" + name + "'}";
    }

}
