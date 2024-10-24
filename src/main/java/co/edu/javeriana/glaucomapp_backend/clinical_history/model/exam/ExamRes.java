package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

import java.util.UUID;

public record ExamRes (UUID examId,String name, String date, String urlImage, Double distanceRatio, Double perimeterRatio,  Double areaRatio){
    
}