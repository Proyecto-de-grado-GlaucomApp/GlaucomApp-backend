package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

import java.util.UUID;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "exam_id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "url_image", nullable = false)
    private String urlImage;

    @Column(name = "distance_ratio", nullable = false)
    private double distanceRatio;

    @Column(name = "perimeter_ratio", nullable = false)
    private double perimeterRatio;

    @Column(name = "area_ratio", nullable = false)
    private double areaRatio;

    @ManyToOne
    @JoinColumn(name = "pacient_id", nullable = false)
    private Pacient pacient;

    @Override
    public String toString() {
        return "Exam [id=" + id + ", name=" + name + ", date=" + date + ", urlImage=" + urlImage + ", distanceRatio="
                + distanceRatio + ", perimeterRatio=" + perimeterRatio + ", areaRatio=" + areaRatio + "]";
    }

}
