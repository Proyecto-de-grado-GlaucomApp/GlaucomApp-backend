package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

/**
 * A record representing an exam request.
 *
 * @param cedula         The identification number of the patient.
 * @param name           The name of the patient.
 * @param date           The date of the exam.
 * @param urlImage       The URL of the image associated with the exam.
 * @param distanceRatio  The distance ratio measured in the exam.
 * @param perimeterRatio The perimeter ratio measured in the exam.
 * @param areaRatio      The area ratio measured in the exam.
 */
public record ExamRequest (String cedula, String name, String date, String urlImage, Double distanceRatio, Double perimeterRatio,  Double areaRatio ){
    
}