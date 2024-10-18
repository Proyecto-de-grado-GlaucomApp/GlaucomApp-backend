package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

import java.util.List;

public record ImageDTO(
    String bitmap,
    List<Double> coordinates,
    List<Double> distances,
    List<Double> perimeters,
    List<Double> areas
) {}