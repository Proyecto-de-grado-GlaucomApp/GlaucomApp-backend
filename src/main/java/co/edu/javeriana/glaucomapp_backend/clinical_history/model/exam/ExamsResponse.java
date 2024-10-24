package co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam;

import java.util.UUID;

public record ExamsResponse (UUID examId, String name, String date, String urlImage ){
    
}