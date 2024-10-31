/**
 * Represents an Exam entity in the system.
 * This entity is used to store information about a medical exam.
 * 
 * Annotations:
 * 
 *   {@link Entity} - Specifies that the class is an entity and is mapped to a database table.
 *   {@link Data} - A Lombok annotation to generate getters, setters, toString, equals, and hashCode methods.
 *   {@link Builder} - A Lombok annotation to implement the builder pattern.
 *   {@link AllArgsConstructor} - A Lombok annotation to generate a constructor with all fields as parameters.
 *   {@link NoArgsConstructor} - A Lombok annotation to generate a no-argument constructor.
 * 
 * 
 * Fields:
 * 
 *   {@code id} - The unique identifier for the exam, generated automatically.
 *   {@code name} - The name of the exam.
 *   {@code date} - The date when the exam was conducted.
 *   {@code urlImage} - The URL of the image associated with the exam.
 *   {@code distanceRatio} - The distance ratio measured in the exam.
 *   {@code perimeterRatio} - The perimeter ratio measured in the exam.
 *   {@code areaRatio} - The area ratio measured in the exam.
 *   {@code pacient} - The patient associated with the exam.
 * 
 * 
 * Methods:
 * 
 *   {@code toString()} - Returns a string representation of the exam object.
 * 
 */
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

    @Column(name = "image_id", nullable = false)
    private String urlImage;

    @Column(name = "distance_ratio", nullable = false)
    private double distanceRatio;

    @Column(name = "perimeter_ratio", nullable = false)
    private double perimeterRatio;

    @Column(name = "area_ratio", nullable = false)
    private double areaRatio;

    @Column(name = "neuroretinal_rim_perimeter", nullable = false)
    private Double neuroretinalRimPerimeter;

    @Column(name = "neuroretinal_rim_area", nullable = false)
    private Double neuroretinalRimArea;

    @Column(name = "cup_perimeter", nullable = false)
    private Double excavationPerimeter;

    @Column(name = "cup_area", nullable = false)
    private Double excavationArea;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "ddl_stage", nullable = false)
    private int ddlStage;

    @ManyToOne
    @JoinColumn(name = "pacient_id", nullable = false)
    private Pacient pacient;

    @Override
    public String toString() {
        return "Exam [id=" + id + ", name=" + name + ", date=" + date + ", urlImage=" + urlImage + ", distanceRatio="
                + distanceRatio + ", perimeterRatio=" + perimeterRatio + ", areaRatio=" + areaRatio + ", neuroretinalRimPerimeter=" 
                + neuroretinalRimPerimeter + ", neuroretinalRimArea=" + neuroretinalRimArea + ", excavationPerimeter=" + excavationPerimeter 
                + ", excavationArea=" + excavationArea + ", state=" + state + ", ddlStage=" + ddlStage + "]";
    }


}
