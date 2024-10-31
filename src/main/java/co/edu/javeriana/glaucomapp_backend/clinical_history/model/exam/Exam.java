/**
 * Represents an exam entity with various attributes related to the exam details.
 * This entity is mapped to a database table using JPA annotations.
 * 
 * Attributes:
 * 
 *   id: Unique identifier for the exam, generated automatically.
 *   name: Name of the exam.
 *   date: Date when the exam was conducted.
 *   urlImage: URL of the image associated with the exam.
 *   distanceRatio: Distance ratio measured in the exam.
 *   perimeterRatio: Perimeter ratio measured in the exam.
 *   areaRatio: Area ratio measured in the exam.
 *   neuroretinalRimPerimeter: Perimeter of the neuroretinal rim.
 *   neuroretinalRimArea: Area of the neuroretinal rim.
 *   excavationPerimeter: Perimeter of the excavation (cup).
 *   excavationArea: Area of the excavation (cup).
 *   state: State or status of the exam.
 *   ddlStage: DDL stage of the exam.
 *   pacient: The patient associated with the exam.
 * 
 * 
 * Annotations:
 * 
 *   @Entity: Specifies that the class is an entity and is mapped to a database table.
 *   @Id: Specifies the primary key of an entity.
 *   @GeneratedValue: Provides for the specification of generation strategies for the values of primary keys.
 *   @Column: Used to specify the mapped column for a persistent property or field.
 *   @ManyToOne: Defines a single-valued association to another entity class that has many-to-one multiplicity.
 *   @JoinColumn: Specifies a column for joining an entity association or element collection.
 * 
 * 
 * Lombok Annotations:
 * 
 *   @Data: Generates getters, setters, toString, equals, and hashCode methods.
 *   @Builder: Produces complex builder APIs for the class.
 *   @AllArgsConstructor: Generates a constructor with 1 parameter for each field in the class.
 *   @NoArgsConstructor: Generates a no-args constructor.
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
