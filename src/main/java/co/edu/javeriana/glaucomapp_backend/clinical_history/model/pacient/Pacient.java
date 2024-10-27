/**
 * Represents a patient in the GlaucomApp system.
 * 
 * This entity is mapped to the "pacient" table in the database and contains
 * information about a patient, including their ID, cedula, name, associated
 * ophthalmologist user, and a list of exams.
 * 
 * Annotations:
 * 
 *   {@link Entity} - Specifies that this class is an entity and is mapped to a database table.
 *   {@link Table} - Specifies the primary table for the annotated entity and unique constraints.
 *   {@link Id} - Specifies the primary key of an entity.
 *   {@link GeneratedValue} - Provides for the specification of generation strategies for the values of primary keys.
 *   {@link Column} - Used to specify the mapped column for a persistent property or field.
 *   {@link ManyToOne} - Defines a single-valued association to another entity class that has many-to-one multiplicity.
 *   {@link JoinColumn} - Specifies a column for joining an entity association or element collection.
 *   {@link OneToMany} - Defines a one-to-many relationship between this entity and another entity.
 *   {@link CascadeType} - Defines the set of cascade operations that are propagated to the associated entity.
 *   {@link lombok.AllArgsConstructor} - Generates a constructor with 1 parameter for each field in the class.
 *   {@link lombok.Builder} - Produces complex builder APIs for your classes.
 *   {@link lombok.Data} - Generates getters for all fields, a useful toString method, and hashCode and equals implementations.
 *   {@link lombok.NoArgsConstructor} - Generates a no-args constructor.
 * 
 * 
 * Fields:
 * 
 *   {@code id} - The unique identifier for the patient, generated automatically.
 *   {@code cedula} - The cedula (identification number) of the patient, cannot be null.
 *   {@code name} - The name of the patient, cannot be null.
 *   {@code ophthalUser} - The ophthalmologist user associated with the patient, cannot be null.
 *   {@code Exams} - The list of exams associated with the patient, with cascade operations and orphan removal enabled.
 * 
 * 
 * Methods:
 * 
 *   {@code toString()} - Returns a string representation of the patient object.
 * 
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient;

import java.util.List;
import java.util.UUID;

import co.edu.javeriana.glaucomapp_backend.auth.exposed.MyUser;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    @JoinColumn(name = "ophtal_id", nullable = false)
    private MyUser ophthalUser;

    @OneToMany(mappedBy = "pacient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exam> Exams;

    @Override
    public String toString() {
        return "Pacient{id=" + id + ", cedula='" + cedula + "', name='" + name + "'}";
    }

}
