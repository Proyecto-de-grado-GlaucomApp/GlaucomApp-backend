package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequestMapping("/glaucoma-screening")
public class GlaucomaScreeningController {

        @Autowired
        private GlaucomaScreeningService glaucomaScreeningService;

        private static final Logger logger = LoggerFactory.getLogger(GlaucomaScreeningService.class);

        /**
         * Processes an image file uploaded (Service test)
         *
         * @param imageFile the image file to be processed
         * @return a ResponseEntity containing the result of the image processing
         */
        @PostMapping(value = "/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ImageProcessingResultDTO> processTestImage(
                        @RequestParam("file") MultipartFile imageFile) {
                System.out.println("Se ha procesado el archivo para probar el servicio");

                // Simulated result of image processing
                ImageProcessingResultDTO result = new ImageProcessingResultDTO();
                result.setImageUrl("http://example.com/processed-image.jpg"); // URL of the processed image
                result.setAreaRatio(0.5); // Area ratio
                result.setPerimeterRatio(0.6); // Perimeter ratio
                result.setDistanceRatio(0.7); // Distance ratio


                return new ResponseEntity<>(result, HttpStatus.OK);
        }

        @GetMapping("/path")
        public ResponseEntity<String> getMethodName() {
                return ResponseEntity.ok("Hello, world!");
        }
        

        @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)

        public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
                if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest().body("No file provided or file is empty."); // Error code 400
                }
                try {
                        ImageProcessingResultDTO processedResult = glaucomaScreeningService.sendImageToApi(file);

                        //AppResultDTO result = glaucomaScreeningService.generateResult();

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
