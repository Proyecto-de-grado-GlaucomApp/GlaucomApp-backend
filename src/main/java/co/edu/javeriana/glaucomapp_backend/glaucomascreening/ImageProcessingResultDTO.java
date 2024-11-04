package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the result of image processing for glaucoma screening.
 *
 * @param imageUrl               The URL of the processed image.
 * @param diagnosticMessage       A diagnostic message generated after processing the image.
 * @param glaucomaLikelihoodPercentage The likelihood percentage of glaucoma based on the analysis.
 * @param ddlsStage              The stage on the Disk Damage Likelihood Scale (DDLS) indicating the severity of damage.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
 public class ImageProcessingResultDTO {

        String imageUrl;
        String imageId;
        Double distanceRatio;
        Double perimeterRatio;
        Double areaRatio;
        Double neuroretinalRimPerimeter;
        Double neuroretinalRimArea;
        Double excavationPerimeter;
        Double excavationArea;
        int state;
        int ddlStage;

}
