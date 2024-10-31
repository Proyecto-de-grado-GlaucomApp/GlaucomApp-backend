package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

public enum GlaucomaStatus {
    AT_RISK("En riesgo"),
    GLAUCOMA_DAMAGE("Daño por glaucoma"),
    GLAUCOMA_DISABILITY("Discapacidad por glaucoma");

    private final String description;

    GlaucomaStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
