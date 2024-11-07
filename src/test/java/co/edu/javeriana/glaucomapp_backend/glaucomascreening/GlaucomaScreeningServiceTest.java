package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import co.edu.javeriana.glaucomapp_backend.s3.exposed.S3Service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GlaucomaScreeningServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GlaucomaScreeningService glaucomaScreeningService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        glaucomaScreeningService.pythonApiUrl = "http://mock-api-url";
    }

    @Test
    public void testSendImageToApi_IOException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("I/O error"));

        assertThrows(RuntimeException.class, () -> glaucomaScreeningService.sendImageToApi(file));
    }
    @Test
    public void testHandleApiResponse_Error() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        byte[] imageBytes = new byte[] { 1, 2, 3 };
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));
        when(file.getBytes()).thenReturn(imageBytes);

        ResponseEntity<String> response = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);

        assertThrows(RuntimeException.class, () -> glaucomaScreeningService.handleApiResponse(file, response));
    }

    @Test
    public void testCalculateDDLStage() {
        assertEquals(1, glaucomaScreeningService.calculateDDLStage(0.4));
        assertEquals(2, glaucomaScreeningService.calculateDDLStage(0.3));
        assertEquals(3, glaucomaScreeningService.calculateDDLStage(0.2));
        assertEquals(4, glaucomaScreeningService.calculateDDLStage(0.1));
        assertEquals(5, glaucomaScreeningService.calculateDDLStage(0.05));
        assertEquals(6, glaucomaScreeningService.calculateDDLStage(0.0));
    }

    @Test
    public void testCalculateState() {
        assertEquals(GlaucomaStatus.AT_RISK.getCode(), glaucomaScreeningService.calculateState(1));
        assertEquals(GlaucomaStatus.GLAUCOMA_DAMAGE.getCode(), glaucomaScreeningService.calculateState(5));
        assertEquals(GlaucomaStatus.GLAUCOMA_DISABILITY.getCode(), glaucomaScreeningService.calculateState(8));
        assertEquals(0, glaucomaScreeningService.calculateState(11));
    }

    @Test
    public void testParseCoordinates() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode coordinates = objectMapper.readTree("[1.0, 2.0]");

        List<Double> result = glaucomaScreeningService.parseCoordinates(coordinates);

        assertEquals(2, result.size());
        assertEquals(1.0, result.get(0));
        assertEquals(2.0, result.get(1));
    }

    @Test
    public void testParseListFromJsonNode() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("[1.0, 2.0]");

        List<Double> result = glaucomaScreeningService.parseListFromJsonNode(jsonNode);

        assertEquals(2, result.size());
        assertEquals(1.0, result.get(0));
        assertEquals(2.0, result.get(1));
    }


    @Test
    public void testPostprocessImage() throws Exception {
        int width = 10;
        int height = 10;
        byte[] data = new byte[width * height * 3]; // Ensure the array size matches the expected size

        BufferedImage result = glaucomaScreeningService.postprocessImage(data, width, height);

        assertNotNull(result);
        assertEquals(width, result.getWidth());
        assertEquals(height, result.getHeight());
    }

    @Test
    public void testGenerateUniqueImageId() {
        String result = glaucomaScreeningService.generateUniqueImageId();

        assertNotNull(result);
        assertTrue(result.startsWith("image_"));
        assertTrue(result.endsWith(".png"));
    }

    @Test
    public void testCalculateRatio() {
        List<Double> values = List.of(2.0, 1.0);

        double result = glaucomaScreeningService.calculateRatio(values);

        assertEquals(0.5, result);
    }

    @Test
    public void testUploadImageToS3() throws Exception {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        ImageProcessingResultDTO processresult = new ImageProcessingResultDTO();
        ServerResultDTO result = new ServerResultDTO();
        result.setPerimeters(List.of(1.0, 2.0));
        result.setAreas(List.of(1.0, 2.0));

        glaucomaScreeningService.uploadImageToS3(image, processresult, result);

        verify(s3Service, times(1)).uploadImage(any(BufferedImage.class), anyString());
        verify(s3Service, times(1)).generatePresignedUrl(anyString());
    }

    @Test
    public void testConfigureObjectMapper() {
        ObjectMapper objectMapper = glaucomaScreeningService.configureObjectMapper();

        assertNotNull(objectMapper);
    }

    @Test
    public void testParseResponse() throws Exception {
        ResponseEntity<String> response = new ResponseEntity<>(
                "{\"image\": {\"bitmap\": \"base64Image\"}, \"coordinates\": [1.0, 2.0], \"distances\": [1.0, 2.0], \"perimeters\": [1.0, 2.0], \"areas\": [1.0, 2.0]}",
                HttpStatus.OK);
        ObjectMapper objectMapper = new ObjectMapper();

        ServerResultDTO result = glaucomaScreeningService.parseResponse(response, objectMapper);

        assertNotNull(result);
    }
}