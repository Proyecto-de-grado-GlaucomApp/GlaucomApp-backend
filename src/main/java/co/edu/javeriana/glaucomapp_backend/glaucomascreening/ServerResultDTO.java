package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerResultDTO {

        private String bitmap;
        private List<Double> coordinates;
        private List<Double> distances;
        private List<Double> perimeters;
        private List<Double> areas;
        private Double spaeth;
        private Double spaethModificadoPerimetro;
        private Double spaethModificadoArea;


}