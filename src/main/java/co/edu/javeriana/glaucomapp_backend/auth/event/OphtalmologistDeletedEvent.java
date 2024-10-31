package co.edu.javeriana.glaucomapp_backend.auth.event;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OphtalmologistDeletedEvent {

    private final UUID ophtalmologistId;

    public OphtalmologistDeletedEvent(UUID ophtalmologistId) {
        this.ophtalmologistId = ophtalmologistId;
    }

    public UUID getOphtalmologistId() {
        return ophtalmologistId;
    }
}