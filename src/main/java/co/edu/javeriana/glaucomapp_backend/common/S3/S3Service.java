package co.edu.javeriana.glaucomapp_backend.common.S3;
import java.awt.image.BufferedImage;

public interface  S3Service {
    String generatePresignedUrl(String objectKey);
    String uploadImage(BufferedImage image, String fileName);
}
