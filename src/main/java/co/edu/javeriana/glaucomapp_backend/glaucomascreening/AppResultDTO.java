package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppResultDTO {
    String imageUrl;
    Double distanceRatio;
    Double perimeterRatio;
    Double areaRatio;

}
