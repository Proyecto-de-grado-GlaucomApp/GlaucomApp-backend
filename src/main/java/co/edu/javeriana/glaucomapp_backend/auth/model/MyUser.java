/**
 * Represents a user entity in the application.
 * This class is mapped to the "OphthalUser" table in the database.
 * It includes fields for user ID, username, password, name, and role.
 * 
 * Annotations:
 * - @Entity: Specifies that the class is an entity and is mapped to a database table.
 * - @Data: Generates getters, setters, toString, equals, and hashCode methods.
 * - @Builder: Provides a builder pattern for object creation.
 * - @AllArgsConstructor: Generates a constructor with one parameter for each field.
 * - @NoArgsConstructor: Generates a no-argument constructor.
 * - @Table: Specifies the primary table for the annotated entity.
 * 
 * Fields:
 * - id: The unique identifier for the user. It is auto-generated.
 * - username: The username of the user. It is unique and cannot be null.
 * - password: The password of the user. It cannot be null.
 * - name: The name of the user. It can be null.
 * - role: The role of the user. It can be null.
 */

package co.edu.javeriana.glaucomapp_backend.auth.model;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "role", nullable = true)
    private String role;
}
