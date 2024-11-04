package co.edu.javeriana.glaucomapp_backend.glaucomascreening;

public enum GlaucomaStatus {
    AT_RISK(1),
    GLAUCOMA_DAMAGE(2),
    GLAUCOMA_DISABILITY(3);

    private final int code;

    GlaucomaStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
