/**
 * Represents a response containing details of an exam.
 * 
 * @param examId  the unique identifier of the exam
 * @param name    the name of the exam
 * @param date    the date when the exam was conducted
 * @param urlImage the URL of the image associated with the exam
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

import java.util.UUID;

public record ExamsResponse (UUID examId, String name, String date, String urlImage ){
    
}