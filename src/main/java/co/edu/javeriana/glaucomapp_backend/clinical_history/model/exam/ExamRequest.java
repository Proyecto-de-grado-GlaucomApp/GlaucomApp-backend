package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

public record ExamRequest (String cedula, String name, String date, String urlImage, Double distanceRatio, Double perimeterRatio,  Double areaRatio ){
    
}