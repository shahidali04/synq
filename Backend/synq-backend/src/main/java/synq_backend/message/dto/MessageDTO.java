package synq_backend.message.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import synq_backend.message.entity.MessageType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

// Represents the message data returned by the API.
@Getter
@Setter
@NoArgsConstructor
public class MessageDTO {

    private UUID id;

    private UUID conversationId;

    private UUID senderId;

    private String content;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;

    private boolean deleted;

    private MessageType type;

    public MessageDTO(
            UUID id,
            UUID conversationId,
            UUID senderId,
            String content,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            LocalDateTime deliveredAt,
            LocalDateTime readAt,
            boolean deleted,
            MessageType type
    ){
        this.id =id;
        this.conversationId = conversationId;
        this.senderId =senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deliveredAt = deliveredAt;
        this.readAt = readAt;
        this.deleted = deleted;
        this.type = type;
    }
}
