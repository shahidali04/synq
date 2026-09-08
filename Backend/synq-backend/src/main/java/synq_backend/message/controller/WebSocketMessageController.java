package synq_backend.message.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import synq_backend.message.dto.MessageDTO;
import synq_backend.message.dto.SendMessageRequest;
import synq_backend.message.dto.WebSocketMessageRequest;
import synq_backend.message.service.MessageService;

import java.security.Principal;
import java.util.UUID;

@Controller
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessageController(
            MessageService messageService,
            SimpMessagingTemplate messagingTemplate
    ){
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    // Receives real-time messages sent by clients.
    @MessageMapping("/chat")
    public void sendMessage(
            WebSocketMessageRequest request,
            Principal principal
    ){

        // Gets the authenticated user's ID from the WebSocket session.
        UUID currentUserId = UUID.fromString(principal.getName());

        SendMessageRequest sendMessageRequest = new SendMessageRequest();

        sendMessageRequest.setConversationId(request.getConversationId());
        sendMessageRequest.setContent(request.getContent());

        // Saves the message through the existing message service.
        MessageDTO savedMessage = messageService.sendMessage(
                currentUserId,
                sendMessageRequest
        );

        // Creates a topic specific to this conversation.
        String destination =
                "/topic/conversation/" + savedMessage.getConversationId();

        // Sends the saved message only to subscribers of this conversation.
        messagingTemplate.convertAndSend(
                destination,
                savedMessage
        );
    }
}