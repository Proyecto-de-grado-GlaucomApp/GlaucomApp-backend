package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;


public class ExamsResponseTest {

    @Test
    public void testExamsResponseCreation() {
        UUID examId = UUID.randomUUID();
        String name = "Eye Exam";
        String date = "2023-10-01";
        String urlImage = "http://example.com/image.jpg";

        ExamsResponse examsResponse = new ExamsResponse(examId, name, date, urlImage);

        assertNotNull(examsResponse);
        assertEquals(examId, examsResponse.examId());
        assertEquals(name, examsResponse.name());
        assertEquals(date, examsResponse.date());
        assertEquals(urlImage, examsResponse.urlImage());
    }
}