package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;

@ExtendWith(MockitoExtension.class)
class GlaucomaScreeningServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BufferedImage mockImage;
    
    @Mock
    private GlaucomaScreeningService glaucomaScreeningService;

    @InjectMocks
    private GlaucomaScreeningService glaucomaScreeningServiceSpy;

    public GlaucomaScreeningServiceTest() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void sendImageToApi_ShouldHandleException_WhenIOExceptionOccurs() throws IOException {
        // Arrange
        when(multipartFile.getInputStream()).thenThrow(new IOException("Mock I/O error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> glaucomaScreeningServiceSpy.sendImageToApi(multipartFile));
        assertThat(thrown.getMessage()).isEqualTo("I/O error while processing the image");
    }

    @Test
    void calculateRatio_ShouldReturnCorrectRatio() {
        // Arrange
        List<Double> values = Arrays.asList(10.0, 20.0);

        // Act
        double ratio = glaucomaScreeningServiceSpy.calculateRatio(values);

        // Assert
        assertThat(ratio).isEqualTo(2.000, withPrecision(0.001));
    }

    @Test
    void generateUniqueImageId_ShouldReturnUniqueId() {
        // Act
        String uniqueId = glaucomaScreeningServiceSpy.generateUniqueImageId();

        // Assert
        assertThat(uniqueId).startsWith("image_");
        assertThat(uniqueId).endsWith(".png");
    }

    @Test
    void postprocessImage_ShouldReturnBufferedImage_WhenDataIsCorrect() throws IOException {
        // Arrange
        byte[] imageData = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        int width = 2;
        int height = 2;
    
        // Act
        BufferedImage image = glaucomaScreeningServiceSpy.postprocessImage(imageData, width, height);
    
        // Assert
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(width);
        assertThat(image.getHeight()).isEqualTo(height);
    }

    @Test
    public void test_preprocess_image_success() throws IOException {
        // Arrange
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        BufferedImage mockImage = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(mockImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();
    
        Mockito.when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));
    
        GlaucomaScreeningService service = new GlaucomaScreeningService(Mockito.mock(S3Service.class));
    
        // Act
        byte[] result = service.preprocessImage(mockFile);
    
        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void handleApiError_ShouldThrowRuntimeException_WhenClientErrorOccurs() {
        // Arrange
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Client error", HttpStatus.BAD_REQUEST);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> glaucomaScreeningServiceSpy.handleApiError(mockResponse));
        assertThat(thrown.getMessage()).isEqualTo("Client error from external API: 400 BAD_REQUEST");
    }

    @Test
    void handleApiError_ShouldThrowRuntimeException_WhenServerErrorOccurs() {
        // Arrange
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Server error", HttpStatus.INTERNAL_SERVER_ERROR);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> glaucomaScreeningServiceSpy.handleApiError(mockResponse));
        assertThat(thrown.getMessage()).isEqualTo("Server error from external API: 500 INTERNAL_SERVER_ERROR");
    }

    @Test
    void calculateDDLStage_ShouldReturnCorrectStage() {
        // Act & Assert
        assertEquals(1, glaucomaScreeningServiceSpy.calculateDDLStage(0.4));
        assertEquals(2, glaucomaScreeningServiceSpy.calculateDDLStage(0.3));
        assertEquals(3, glaucomaScreeningServiceSpy.calculateDDLStage(0.2));
        assertEquals(4, glaucomaScreeningServiceSpy.calculateDDLStage(0.1));
        assertEquals(5, glaucomaScreeningServiceSpy.calculateDDLStage(0.05));
        assertEquals(6, glaucomaScreeningServiceSpy.calculateDDLStage(0.0));
    }

    @Test
    void calculateState_ShouldReturnCorrectState() {
        // Act & Assert
        assertEquals(GlaucomaStatus.AT_RISK.getCode(), glaucomaScreeningServiceSpy.calculateState(1));
        assertEquals(GlaucomaStatus.AT_RISK.getCode(), glaucomaScreeningServiceSpy.calculateState(2));
        assertEquals(GlaucomaStatus.AT_RISK.getCode(), glaucomaScreeningServiceSpy.calculateState(3));
        assertEquals(GlaucomaStatus.AT_RISK.getCode(), glaucomaScreeningServiceSpy.calculateState(4));
        assertEquals(GlaucomaStatus.GLAUCOMA_DAMAGE.getCode(), glaucomaScreeningServiceSpy.calculateState(5));
        assertEquals(GlaucomaStatus.GLAUCOMA_DAMAGE.getCode(), glaucomaScreeningServiceSpy.calculateState(6));
        assertEquals(GlaucomaStatus.GLAUCOMA_DISABILITY.getCode(), glaucomaScreeningServiceSpy.calculateState(8));
    }
  
            
}