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

public class GlaucomaScreeningMobileControllerTest {


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
        ImageProcessingResultDTO expectedResult = new ImageProcessingResultDTO();
        when(glaucomaScreeningService.sendImageToApi(mockFile)).thenReturn(expectedResult);
        
        ResponseEntity<?> response = glaucomaScreeningController.uploadImage(mockFile);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    // Test para manejar casos de archivo no proporcionado o nulo
    @Test
    public void test_upload_image_no_file_returns_400() {
        // Simular archivo vacío
        when(mockFile.isEmpty()).thenReturn(true);
        ResponseEntity<?> responseEmpty = glaucomaScreeningController.uploadImage(mockFile);
        
        assertEquals(HttpStatus.BAD_REQUEST, responseEmpty.getStatusCode());
        assertEquals("No file provided or file is empty.", responseEmpty.getBody());
        
        // Simular archivo nulo
        ResponseEntity<?> responseNull = glaucomaScreeningController.uploadImage(null);
        
        assertEquals(HttpStatus.BAD_REQUEST, responseNull.getStatusCode());
        assertEquals("No file provided or file is empty.", responseNull.getBody());
    }

    // Test para manejar errores del servicio
    @Test
    public void test_upload_image_service_error_returns_400_or_500() {
        // Error de cliente
        String clientErrorMessage = "Client error: Invalid input";
        doThrow(new RuntimeException(clientErrorMessage)).when(glaucomaScreeningService).sendImageToApi(any(MultipartFile.class));
        
        ResponseEntity<?> responseClientError = glaucomaScreeningController.uploadImage(mockFile);
        assertEquals(HttpStatus.BAD_REQUEST, responseClientError.getStatusCode());
        assertEquals("Request error: " + clientErrorMessage, responseClientError.getBody());

        // Error de servidor
        String serverErrorMessage = "Server error: Service unavailable";
        doThrow(new RuntimeException(serverErrorMessage)).when(glaucomaScreeningService).sendImageToApi(any(MultipartFile.class));
        
        ResponseEntity<?> responseServerError = glaucomaScreeningController.uploadImage(mockFile);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseServerError.getStatusCode());
        assertEquals("External service error: " + serverErrorMessage, responseServerError.getBody());

        // Error inesperado
        String unexpectedErrorMessage = "Unexpected error occurred";
        doThrow(new RuntimeException(unexpectedErrorMessage)).when(glaucomaScreeningService).sendImageToApi(any(MultipartFile.class));
        
        ResponseEntity<?> responseUnexpectedError = glaucomaScreeningController.uploadImage(mockFile);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseUnexpectedError.getStatusCode());
        assertEquals("An unexpected error occurred.", responseUnexpectedError.getBody());
    }
}
