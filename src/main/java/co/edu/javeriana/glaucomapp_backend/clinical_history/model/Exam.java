package co.edu.javeriana.glaucomapp_backend.clinical_history.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "exam_id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    private UUID id;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "url_image", nullable = false)
    private String urlImage;

    @Column(name = "confiablity", nullable = false)
    private String confiability;

    @Column(name = "description", nullable = false)
    private double description;

    @Column(name = "spaeth_scale", nullable = false)
    private double spaethScale;






    
    
}
