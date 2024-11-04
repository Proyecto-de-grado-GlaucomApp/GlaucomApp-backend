package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequestMapping("/api/v1/glaucoma-screening")
@RequiredArgsConstructor
public class GlaucomaScreeningController {

        
        private final GlaucomaScreeningService glaucomaScreeningService;

        private static final Logger logger = LoggerFactory.getLogger(GlaucomaScreeningService.class);
       
        @PostMapping("/upload-image")
        public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
                System.out.println("Received request to upload image");
                if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest().body("No file provided or file is empty."); // Error code 400
                }
                try {
                        ImageProcessingResultDTO processedResult = glaucomaScreeningService.sendImageToApi(file);

                        return new ResponseEntity<>(processedResult, HttpStatus.OK);

                } catch (RuntimeException e) {
                        if (e.getMessage().contains("Client error")) {
                                logger.error("Client error: " + e.getMessage());
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("Request error: " + e.getMessage());
                        } else if (e.getMessage().contains("Server error")) {
                                logger.error("Server error: " + e.getMessage());
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body("External service error: " + e.getMessage());
                        } else {
                                logger.error("Unexpected error: " + e.getMessage());
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body("An unexpected error occurred.");
                        }
                }
        }

}
