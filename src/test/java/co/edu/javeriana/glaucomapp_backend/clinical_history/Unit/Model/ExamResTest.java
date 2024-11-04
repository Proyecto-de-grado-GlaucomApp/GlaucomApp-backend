package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;

public class ExamResTest {

    @Test
    public void testExamResCreation() {
        UUID examId = UUID.randomUUID();
        String name = "Eye Exam";
        String date = "2023-10-01";
        String urlImage = "http://example.com/image.jpg";
        Double distanceRatio = 1.2;
        Double perimeterRatio = 0.8;
        Double areaRatio = 0.5;
        Double neuroretinalRimPerimeter = 2.3;
        Double neuroretinalRimArea = 1.1;
        Double excavationPerimeter = 0.9;
        Double excavationArea = 0.4;
        String state = "Completed";
        int ddlStage = 3;

        ExamRes examRes = new ExamRes(examId, name, date, urlImage, distanceRatio, perimeterRatio, areaRatio, neuroretinalRimPerimeter, neuroretinalRimArea, excavationPerimeter, excavationArea, state, ddlStage);

        assertNotNull(examRes);
        assertEquals(examId, examRes.examId());
        assertEquals(name, examRes.name());
        assertEquals(date, examRes.date());
        assertEquals(urlImage, examRes.urlImage());
        assertEquals(distanceRatio, examRes.distanceRatio());
        assertEquals(perimeterRatio, examRes.perimeterRatio());
        assertEquals(areaRatio, examRes.areaRatio());
        assertEquals(neuroretinalRimPerimeter, examRes.neuroretinalRimPerimeter());
        assertEquals(neuroretinalRimArea, examRes.neuroretinalRimArea());
        assertEquals(excavationPerimeter, examRes.excavationPerimeter());
        assertEquals(excavationArea, examRes.excavationArea());
        assertEquals(state, examRes.state());
        assertEquals(ddlStage, examRes.ddlStage());
    }
}