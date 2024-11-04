package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class GlaucomaScreeningControllerTest {

    @InjectMocks
    private GlaucomaScreeningController glaucomaScreeningController;

    @Mock
    private GlaucomaScreeningService glaucomaScreeningService;

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

        // Test de éxito para la carga de imagen
    @Test
    public void test_upload_image_success_returns_200() {
        // Arrange
        ImageProcessingResultDTO expectedResult = new ImageProcessingResultDTO(); // Crea un objeto de resultado esperado
        when(glaucomaScreeningService.sendImageToApi(mockFile)).thenReturn(expectedResult);
        
        // Act
        ResponseEntity<?> response = glaucomaScreeningController.uploadImage(mockFile);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }


        // Test para manejar el caso cuando no se proporciona un archivo
        @Test
        public void test_upload_image_no_file_provided_returns_400() {
            // Arrange
            when(mockFile.isEmpty()).thenReturn(true);
    
            // Act
            ResponseEntity<?> response = glaucomaScreeningController.uploadImage(mockFile);
    
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("No file provided or file is empty.", response.getBody());
        }
    
        // Test para manejar el caso cuando el archivo es nulo
        @Test
        public void test_upload_image_null_file_returns_400() {
            // Act
            ResponseEntity<?> response = glaucomaScreeningController.uploadImage(null);
    
            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("No file provided or file is empty.", response.getBody());
        }

        
    // Test para manejar errores de cliente
    @Test
    public void test_upload_image_client_error_returns_400() {
        // Arrange
        String errorMessage = "Client error: Invalid input";
        doThrow(new RuntimeException(errorMessage)).when(glaucomaScreeningService).sendImageToApi(any(MultipartFile.class));
        
        // Act
        ResponseEntity<?> response = glaucomaScreeningController.uploadImage(mockFile);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request error: " + errorMessage, response.getBody());
    }

    // Test para manejar errores de servidor
    @Test
    public void test_upload_image_server_error_returns_500() {
        // Arrange
        String errorMessage = "Server error: Service unavailable";
        doThrow(new RuntimeException(errorMessage)).when(glaucomaScreeningService).sendImageToApi(any(MultipartFile.class));
        
        // Act
        ResponseEntity<?> response = glaucomaScreeningController.uploadImage(mockFile);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("External service error: " + errorMessage, response.getBody());
    }

    // Test para manejar errores inesperados
    @Test
    public void test_upload_image_unexpected_error_returns_500() {
        // Arrange
        String errorMessage = "Unexpected error occurred";
        doThrow(new RuntimeException(errorMessage)).when(glaucomaScreeningService).sendImageToApi(any(MultipartFile.class));
        
        // Act
        ResponseEntity<?> response = glaucomaScreeningController.uploadImage(mockFile);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred.", response.getBody());
    }
}
