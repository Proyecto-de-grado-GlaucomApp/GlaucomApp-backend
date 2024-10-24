package co.edu.javeriana.glaucomapp_backend.clinical_history.model;

import java.util.UUID;

import co.edu.javeriana.glaucomapp_backend.auth.model.MyUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pacient", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "pacient_id" }) })
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

}
