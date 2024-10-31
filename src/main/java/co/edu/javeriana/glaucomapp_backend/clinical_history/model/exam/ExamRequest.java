/**
 * Represents a request for an exam in the GlaucomApp system.
 *
 * @param cedula The identification number of the patient.
 * @param name The name of the patient.
 * @param date The date when the exam was conducted.
 * @param urlImage The URL of the image associated with the exam.
 * @param distanceRatio The distance ratio measured in the exam.
 * @param perimeterRatio The perimeter ratio measured in the exam.
 * @param areaRatio The area ratio measured in the exam.
 * @param neuroretinalRimPerimeter The perimeter of the neuroretinal rim measured in the exam.
 * @param neuroretinalRimArea The area of the neuroretinal rim measured in the exam.
 * @param excavationPerimeter The perimeter of the excavation measured in the exam.
 * @param excavationArea The area of the excavation measured in the exam.
 * @param state The state of the exam.
 * @param ddlStage The stage of the disease as determined by the exam.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

public record ExamRequest (String cedula, String name, String date, String urlImage, Double distanceRatio, Double perimeterRatio,  Double areaRatio,  Double neuroretinalRimPerimeter, Double neuroretinalRimArea, Double excavationPerimeter, Double excavationArea, String state, int ddlStage) {
    
}