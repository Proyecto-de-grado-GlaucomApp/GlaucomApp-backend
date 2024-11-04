package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;

class GlaucomaScreeningServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    @InjectMocks
    private GlaucomaScreeningService glaucomaScreeningService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        
        MockitoAnnotations.openMocks(this);
    }

    // Successfully preprocesses an image file into a byte array
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

        // Handles null or empty MultipartFile input gracefully
    @Test
    public void test_preprocess_image_with_null_or_empty_file() {
        // Arrange
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.isEmpty()).thenReturn(true);
    
        GlaucomaScreeningService service = new GlaucomaScreeningService(Mockito.mock(S3Service.class));
    
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.preprocessImage(mockFile);
        });
    }

        
}
