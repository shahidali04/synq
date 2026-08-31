package synq_backend.conversation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Contains the data required to create a direct conversation.
@Getter
@Setter
@NoArgsConstructor
public class ConversationRequest {

    // ID of the user with whom the conversation should be created.
    private UUID userId;
}
