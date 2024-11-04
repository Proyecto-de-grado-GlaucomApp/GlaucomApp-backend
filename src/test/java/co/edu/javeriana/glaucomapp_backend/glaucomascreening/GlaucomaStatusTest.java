package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GlaucomaStatusTest {

    @Test
    void testAtRiskCode() {
        assertEquals(1, GlaucomaStatus.AT_RISK.getCode(), "Código de AT_RISK debe ser 1");
    }

    @Test
    void testGlaucomaDamageCode() {
        assertEquals(2, GlaucomaStatus.GLAUCOMA_DAMAGE.getCode(), "Código de GLAUCOMA_DAMAGE debe ser 2");
    }

    @Test
    void testGlaucomaDisabilityCode() {
        assertEquals(3, GlaucomaStatus.GLAUCOMA_DISABILITY.getCode(), "Código de GLAUCOMA_DISABILITY debe ser 3");
    }
}
