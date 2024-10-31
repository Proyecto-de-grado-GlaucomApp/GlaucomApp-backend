package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ImageProcessingResultDTOTest {

        @Test
        void testConstructorAndGetters() {
                // Arrange
                String imageUrl = "http://example.com/image.jpg";
                Double distanceRatio = 0.5;
                Double perimeterRatio = 0.6;
                Double areaRatio = 0.7;

                // Act
                ImageProcessingResultDTO result = new ImageProcessingResultDTO();
                result.setImageUrl(imageUrl);
                result.setDistanceRatio(distanceRatio);
                result.setPerimeterRatio(perimeterRatio);
                result.setAreaRatio(areaRatio);

                // Assert
                assertEquals(imageUrl, result.getImageUrl());
                assertEquals(distanceRatio, result.getDistanceRatio());
                assertEquals(perimeterRatio, result.getPerimeterRatio());
                assertEquals(areaRatio, result.getAreaRatio());
        }

        @Test
        void testConstructorWithDifferentValues() {
                // Arrange
                String imageUrl = "http://example.com/image.jpg";
                Double distanceRatio = 0.5;
                Double perimeterRatio = 0.6;
                Double areaRatio = 0.7;

                // Act
                ImageProcessingResultDTO result = new ImageProcessingResultDTO();
                result.setImageUrl(imageUrl);
                result.setDistanceRatio(distanceRatio);
                result.setPerimeterRatio(perimeterRatio);
                result.setAreaRatio(areaRatio);


                // Assert
                assertEquals(imageUrl, result.getImageUrl());
                assertEquals(distanceRatio, result.getDistanceRatio());
                assertEquals(perimeterRatio, result.getPerimeterRatio());
                assertEquals(areaRatio, result.getAreaRatio());
                
        }
}