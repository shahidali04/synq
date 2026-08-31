package synq_backend.conversation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

// Represents the conversation data returned by the API.
@Getter
@Setter
@NoArgsConstructor
public class ConversationDTO {

    private UUID id;

    private String type;

    private UUID createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ConversationDTO(
            UUID id,
            String type,
            UUID createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
    }
}
