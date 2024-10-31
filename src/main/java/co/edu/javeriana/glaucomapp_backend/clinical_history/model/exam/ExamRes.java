/**
 * Represents the response of an exam in the GlaucomApp system.
 * 
 * @param examId Unique identifier for the exam.
 * @param name Name of the exam.
 * @param date Date when the exam was conducted.
 * @param urlImage URL of the image associated with the exam.
 * @param distanceRatio Ratio of distances measured in the exam.
 * @param perimeterRatio Ratio of perimeters measured in the exam.
 * @param areaRatio Ratio of areas measured in the exam.
 * @param neuroretinalRimPerimeter Perimeter of the neuroretinal rim.
 * @param neuroretinalRimArea Area of the neuroretinal rim.
 * @param excavationPerimeter Perimeter of the excavation.
 * @param excavationArea Area of the excavation.
 * @param state Current state of the exam.
 * @param ddlStage Stage of the exam in the DDL (Data Definition Language) process.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

import java.util.UUID;

public record ExamRes (UUID examId, String name, String date, String urlImage, Double distanceRatio, Double perimeterRatio,  Double areaRatio, Double neuroretinalRimPerimeter,Double neuroretinalRimArea, Double excavationPerimeter, Double excavationArea,String state,  int ddlStage ){
    
}