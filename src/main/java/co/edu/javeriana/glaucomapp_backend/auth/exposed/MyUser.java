
/**
 * Represents a user entity in the GlaucomApp backend system.
 * This entity is mapped to the "OphthalUser" table in the database.
 * Each user has a unique ID, username, password, name, and an optional role.
 * A user can have multiple associated patients.
 * 
 * Annotations:
 * - @Entity: Specifies that this class is an entity and is mapped to a database table.
 * - @Data: Lombok annotation to generate getters, setters, toString, equals, and hashCode methods.
 * - @Builder: Lombok annotation to implement the builder pattern.
 * - @AllArgsConstructor: Lombok annotation to generate a constructor with all fields.
 * - @NoArgsConstructor: Lombok annotation to generate a no-argument constructor.
 * - @Table: Specifies the table name in the database.
 * - @Id: Specifies the primary key of the entity.
 * - @GeneratedValue: Specifies the generation strategy for the primary key.
 * - @Column: Specifies the column details in the database table.
 * - @OneToMany: Specifies a one-to-many relationship with the Pacient entity.
 * 
 * Fields:
 * - id: Unique identifier for the user, generated automatically.
 * - username: Unique username for the user, cannot be null.
 * - password: Password for the user, cannot be null.
 * - name: Name of the user, cannot be null.
 * - role: Role of the user, can be null.
 * - pacients: List of patients associated with the user.
 * 
 * Methods:
 * - toString: Returns a string representation of the user entity.
 */
package co.edu.javeriana.glaucomapp_backend.auth.exposed;

import java.util.List;
import java.util.UUID;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "OphthalUser")
public class MyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ophtal_id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "role", nullable = true)
    private String role;

    @Override
    public String toString() {
        return "MyUser{id=" + id + ", username='" + username + "', name='" + name + "', role='" + role + "'}";
    }
}
