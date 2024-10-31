/**
 * Represents a user entity in the OphthalUser table.
 * This class is annotated with JPA annotations to map it to a database table.
 * It includes fields for the user's ID, username, password, name, and role.
 * 
 * Annotations:
 * - @Entity: Specifies that the class is an entity and is mapped to a database table.
 * - @Table: Specifies the name of the database table to be used for mapping.
 * - @Id: Specifies the primary key of an entity.
 * - @GeneratedValue: Provides for the specification of generation strategies for the values of primary keys.
 * - @Column: Specifies the mapped column for a persistent property or field.
 * - @Data: A Lombok annotation to generate getters, setters, toString, equals, and hashCode methods.
 * - @Builder: A Lombok annotation to implement the builder pattern.
 * - @AllArgsConstructor: A Lombok annotation to generate a constructor with 1 parameter for each field in the class.
 * - @NoArgsConstructor: A Lombok annotation to generate a constructor with no parameters.
 * 
 * Fields:
 * - id: The unique identifier for the user, generated automatically as a UUID.
 * - username: The username of the user, must be unique and not null.
 * - password: The password of the user, must not be null.
 * - name: The name of the user, must not be null.
 * - role: The role of the user, can be null.
 * 
 * Overrides:
 * - toString: Provides a string representation of the user object.
 */
package co.edu.javeriana.glaucomapp_backend.auth.exposed;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
