/**
 * Represents the response for an exam in the clinical history.
 * 
 * @param examId The unique identifier of the exam.
 * @param name The name of the exam.
 * @param date The date when the exam was conducted.
 * @param urlImage The URL of the image associated with the exam.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

import java.util.UUID;

public record ExamsResponse (UUID examId, String name, String date, String urlImage ){
    
}