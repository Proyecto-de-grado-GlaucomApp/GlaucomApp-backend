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
