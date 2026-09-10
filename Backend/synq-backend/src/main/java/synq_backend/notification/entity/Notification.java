package synq_backend.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import synq_backend.user.entity.User;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    // User who will receive this notification.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Type of event that generated the notification.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // Human-readable notification message.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // ID of the related resource, such as a conversation or group.
    @Column(name = "reference_id")
    private UUID referenceId;

    // Indicates whether the user has read the notification.
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    // Time when the notification was created.
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Notification(
            User user,
            NotificationType type,
            String message,
            UUID referenceId
    ){
        this.user = user;
        this.type = type;
        this.message = message;
        this.referenceId = referenceId;
        this.createdAt = OffsetDateTime.now();
    }

}
