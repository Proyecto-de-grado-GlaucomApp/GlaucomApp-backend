package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

/**
 * Data Transfer Object representing the result of image processing for glaucoma screening.
 *
 * @param imageUrl               The URL of the processed image.
 * @param diagnosticMessage       A diagnostic message generated after processing the image.
 * @param glaucomaLikelihoodPercentage The likelihood percentage of glaucoma based on the analysis.
 * @param ddlsStage              The stage on the Disk Damage Likelihood Scale (DDLS) indicating the severity of damage.
 */
public record ImageProcessingResultDTO(

        String imageUrl,
        String diagnosticMessage,
        int glaucomaLikelihoodPercentage,
        int ddlsStage
) {
}
