/**
 * Represents the response of an exam in the clinical history.
 * 
 * @param examId        Unique identifier for the exam.
 * @param name          Name of the exam.
 * @param date          Date when the exam was conducted.
 * @param urlImage      URL of the image associated with the exam.
 * @param distanceRatio Ratio of the distance measurement in the exam.
 * @param perimeterRatio Ratio of the perimeter measurement in the exam.
 * @param areaRatio     Ratio of the area measurement in the exam.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

import java.util.UUID;

public record ExamRes (UUID examId,String name, String date, String urlImage, Double distanceRatio, Double perimeterRatio,  Double areaRatio){
    
}