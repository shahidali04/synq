package synq_backend.message.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private boolean deleted;

    public MessageDTO(
            UUID id,
            UUID conversationId,
            UUID senderId,
            String content,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            boolean deleted
    ){
        this.id =id;
        this.conversationId = conversationId;
        this.senderId =senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }
}
