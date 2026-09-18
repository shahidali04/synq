package synq_backend.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

// Represents a WebSocket event sent to a user when their
// subscription to a group conversation needs to be removed.
@Getter
@AllArgsConstructor
public class GroupSubscriptionEvent {

    private String type;
    private UUID conversationId;
}
