package co.edu.javeriana.glaucomapp_backend.s3;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
public class S3ServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3ServiceImpl s3Service;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String TEST_FILE_NAME = "test-file.png";
    private static final String TEST_URL = "https://test-bucket.s3.amazonaws.com/test-file.png";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    }

    @Test
    void generatePresignedUrl_Success() throws Exception {
        // Arrange
        String objectKey = TEST_FILE_NAME;
        URL expectedUrl = new URL(TEST_URL);
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        
        when(presignedRequest.url()).thenReturn(expectedUrl);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .thenReturn(presignedRequest);

        // Act
        String result = s3Service.generatePresignedUrl(objectKey);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_URL, result);
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void uploadImage_Success() throws IOException {
        // Arrange
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        S3Utilities s3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(new URL(TEST_URL));
        
        // Act
        String result = s3Service.uploadImage(testImage, TEST_FILE_NAME);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_URL, result);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    void deleteImage_Success() {
        // Act
        String result = s3Service.deleteImage(TEST_FILE_NAME);

        // Assert
        assertEquals("File deleted successfully: " + TEST_FILE_NAME, result);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteImage_Failure() {
        // Arrange
        doThrow(new RuntimeException("Delete failed"))
            .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // Act
        String result = s3Service.deleteImage(TEST_FILE_NAME);

        // Assert
        assertTrue(result.startsWith("Error deleting file:"));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}