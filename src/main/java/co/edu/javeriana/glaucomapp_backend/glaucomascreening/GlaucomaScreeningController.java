package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for handling glaucoma screening requests.
 * <p>
 * This controller provides endpoints for processing images related
 * to glaucoma screening, both from mobile applications and third-party
 * services. It currently simulates image processing results.
 * </p>
 */
@RestController
@RequestMapping("/glaucoma-screening")
public class GlaucomaScreeningController {

    /**
     * Processes an image file uploaded from a mobile device.
     *
     * @param imageFile the image file to be processed
     * @return a ResponseEntity containing the result of the image processing
     */
    @PostMapping(value = "/mobile", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageProcessingResultDTO> processMobileImage(
            @RequestParam("file") MultipartFile imageFile) {
        // TODO: Implement actual image processing logic here

        // Simulated result of image processing
        ImageProcessingResultDTO result = new ImageProcessingResultDTO(
                "http://example.com/processed-image.jpg",  // URL of the processed image
                "Imagen procesada correctamente.",          // Diagnostic message
                85,                                         // Percentage likelihood of glaucoma
                2                                           // Stage on the Damage of Disc Scale (DDLS)
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Processes an image file uploaded from a third-party service.
     *
     * @param imageFile the image file to be processed
     * @return a ResponseEntity containing the result of the image processing
     */
    @PostMapping(value = "/third-party", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageProcessingResultDTO> processThirdPartyImage(
            @RequestParam("file") MultipartFile imageFile) {
        // TODO: Implement actual image processing logic here

        // Simulated result of image processing
        ImageProcessingResultDTO result = new ImageProcessingResultDTO(
                "http://example.com/processed-image-third-party.jpg",  // URL of the processed image
                "Imagen procesada correctamente para terceros.",          // Diagnostic message
                90,                                                       // Percentage likelihood of glaucoma
                3                                                         // Stage on the Damage of Disc Scale (DDLS)
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
