package co.edu.javeriana.glaucomapp_backend.clinical_history.Unit.Model;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.Exam;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.Pacient;

public class ExamTest {

    private Exam exam;
    private Pacient pacient;

    @BeforeEach
    public void setUp() {
        pacient = new Pacient();
        exam = Exam.builder()
                .id(UUID.randomUUID())
                .name("Eye Exam")
                .date("2023-10-01")
                .urlImage("http://example.com/image.jpg")
                .distanceRatio(1.5)
                .perimeterRatio(2.5)
                .areaRatio(3.5)
                .neuroretinalRimPerimeter(4.5)
                .neuroretinalRimArea(5.5)
                .excavationPerimeter(6.5)
                .excavationArea(7.5)
                .state("Completed")
                .ddlStage(1)
                .pacient(pacient)
                .build();
    }

    @Test
    public void testExamNotNull() {
        assertNotNull(exam);
    }

    @Test
    public void testExamAttributes() {
        assertEquals("Eye Exam", exam.getName());
        assertEquals("2023-10-01", exam.getDate());
        assertEquals("http://example.com/image.jpg", exam.getUrlImage());
        assertEquals(1.5, exam.getDistanceRatio());
        assertEquals(2.5, exam.getPerimeterRatio());
        assertEquals(3.5, exam.getAreaRatio());
        assertEquals(4.5, exam.getNeuroretinalRimPerimeter());
        assertEquals(5.5, exam.getNeuroretinalRimArea());
        assertEquals(6.5, exam.getExcavationPerimeter());
        assertEquals(7.5, exam.getExcavationArea());
        assertEquals("Completed", exam.getState());
        assertEquals(1, exam.getDdlStage());
        assertEquals(pacient, exam.getPacient());
    }

    @Test
    public void testToString() {
        String expected = "Exam [id=" + exam.getId() + ", name=Eye Exam, date=2023-10-01, urlImage=http://example.com/image.jpg, distanceRatio=1.5, perimeterRatio=2.5, areaRatio=3.5, neuroretinalRimPerimeter=4.5, neuroretinalRimArea=5.5, excavationPerimeter=6.5, excavationArea=7.5, state=Completed, ddlStage=1]";
        assertEquals(expected, exam.toString());
    }
}
