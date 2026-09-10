package synq_backend.notification.dto;

import synq_backend.notification.entity.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

// Represents notification data returned to the client.
public class NotificationDTO {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String message;
    private UUID referenceId;
    private boolean read;
    private OffsetDateTime createdAt;

    public NotificationDTO(
            UUID id,
            UUID userId,
            NotificationType type,
            String message,
            UUID referenceId,
            boolean read,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.referenceId = referenceId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public boolean isRead() {
        return read;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}