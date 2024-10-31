package co.edu.javeriana.glaucomapp_backend.common.S3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import java.io.ByteArrayInputStream;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;


@Service
public class S3ServiceImpl implements S3Service{

        @Value("${AWS_BUCKET_NAME}")
    private String bucketName;
    
    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    public S3ServiceImpl(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }


    @Override
    public String generatePresignedUrl(String objectKey) {
        // Crear la solicitud para obtener el objeto
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        // Crear la solicitud para presignar el objeto
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1)) // Duración de la firma
                .getObjectRequest(getObjectRequest) // Solicitud para el objeto
                .build();

        // Generar la URL firmada
        URL presignedUrl = s3Presigner.presignGetObject(getObjectPresignRequest).url();
        return presignedUrl.toString(); // Retorna la URL como cadena
    }

    @Override
        public String uploadImage(BufferedImage image, String fileName) {
        System.out.println("Uploading image to S3");
        try {
            // Convertir BufferedImage a InputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos); // Puedes cambiar "png" por el formato deseado
            InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
            System.out.println("InputStream: ");

            // Crear la solicitud para subir el objeto con tipo de contenido
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/png") // Cambia esto según el tipo de imagen
                    .build();
            System.out.println("Bucket: " + bucketName);
            // Subir el objeto
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, baos.size()));
            
            System.out.println("File uploaded to S3");

            // Generar la URL del archivo subido
            String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build()).toString();
            
            System.out.println("File URL: " + fileUrl);

            return fileUrl; // Retorna la URL del archivo
        } catch (IOException e) {
            return "Error uploading file: " + e.getMessage();
        }
    }

    @Override
    public String deleteImage(String fileName) {
        try {
            // Crear la solicitud para eliminar el objeto
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            // Ejecutar la solicitud para eliminar el objeto
            s3Client.deleteObject(deleteObjectRequest);

            return "File deleted successfully: " + fileName;
        } catch (Exception e) {
            return "Error deleting file: " + e.getMessage();
        }
    }
}
