package synq_backend.message.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import synq_backend.message.dto.GroupSubscriptionEvent;

import java.util.UUID;

// Handles application-level WebSocket events used by Synq.
@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(
            SimpMessagingTemplate messagingTemplate
    ){
        this.messagingTemplate = messagingTemplate;
    }

    public void disconnectFromConversation(
            UUID userId,
            UUID conversationId
    ){
        GroupSubscriptionEvent event =
                new GroupSubscriptionEvent(
                "FORCE_UNSUBSCRIBE",
                conversationId );

        messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/conversation-events",
                event
        );
    }
}
