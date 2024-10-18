package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ImageProcessingResultDTOTest {

        @Test
        void testConstructorAndGetters() {
                // Arrange
                String imageUrl = "http://example.com/image.jpg";
                String diagnosticMessage = "No signs of glaucoma.";
                int glaucomaLikelihoodPercentage = 10;
                int ddlsStage = 1;

                // Act
                ImageProcessingResultDTO result = new ImageProcessingResultDTO(
                                imageUrl, diagnosticMessage, glaucomaLikelihoodPercentage, ddlsStage);

                // Assert
                assertEquals(imageUrl, result.imageUrl());
                assertEquals(diagnosticMessage, result.diagnosticMessage());
                assertEquals(glaucomaLikelihoodPercentage, result.glaucomaLikelihoodPercentage());
                assertEquals(ddlsStage, result.ddlsStage());
        }

        @Test
        void testConstructorWithDifferentValues() {
                // Arrange
                String imageUrl = "http://example.com/another_image.jpg";
                String diagnosticMessage = "Signs of early glaucoma.";
                int glaucomaLikelihoodPercentage = 50;
                int ddlsStage = 2;

                // Act
                ImageProcessingResultDTO result = new ImageProcessingResultDTO(
                                imageUrl, diagnosticMessage, glaucomaLikelihoodPercentage, ddlsStage);

                // Assert
                assertEquals(imageUrl, result.imageUrl());
                assertEquals(diagnosticMessage, result.diagnosticMessage());
                assertEquals(glaucomaLikelihoodPercentage, result.glaucomaLikelihoodPercentage());
                assertEquals(ddlsStage, result.ddlsStage());
        }
}