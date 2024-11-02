package co.edu.javeriana.glaucomapp_backend.s3.exposed;
import java.awt.image.BufferedImage;

public interface  S3Service {
    String generatePresignedUrl(String objectKey);
    String uploadImage(BufferedImage image, String fileName);
    String deleteImage(String fileName);
}
