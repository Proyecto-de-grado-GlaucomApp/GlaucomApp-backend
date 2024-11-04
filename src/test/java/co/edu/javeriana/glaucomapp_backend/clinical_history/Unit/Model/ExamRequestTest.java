package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;


public class ExamRequestTest {

    @Test
    public void testExamRequestCreation() {
        String cedula = "123456789";
        String name = "John Doe";
        String date = "2023-10-01";
        String urlImage = "http://example.com/image.jpg";
        Double distanceRatio = 1.2;
        Double perimeterRatio = 2.3;
        Double areaRatio = 3.4;
        Double neuroretinalRimPerimeter = 4.5;
        Double neuroretinalRimArea = 5.6;
        Double excavationPerimeter = 6.7;
        Double excavationArea = 7.8;
        String state = "Normal";
        int ddlStage = 2;

        ExamRequest examRequest = new ExamRequest(cedula, name, date, urlImage, distanceRatio, perimeterRatio, areaRatio, neuroretinalRimPerimeter, neuroretinalRimArea, excavationPerimeter, excavationArea, state, ddlStage);

        assertNotNull(examRequest);
        assertEquals(cedula, examRequest.cedula());
        assertEquals(name, examRequest.name());
        assertEquals(date, examRequest.date());
        assertEquals(urlImage, examRequest.urlImage());
        assertEquals(distanceRatio, examRequest.distanceRatio());
        assertEquals(perimeterRatio, examRequest.perimeterRatio());
        assertEquals(areaRatio, examRequest.areaRatio());
        assertEquals(neuroretinalRimPerimeter, examRequest.neuroretinalRimPerimeter());
        assertEquals(neuroretinalRimArea, examRequest.neuroretinalRimArea());
        assertEquals(excavationPerimeter, examRequest.excavationPerimeter());
        assertEquals(excavationArea, examRequest.excavationArea());
        assertEquals(state, examRequest.state());
        assertEquals(ddlStage, examRequest.ddlStage());
    }
}