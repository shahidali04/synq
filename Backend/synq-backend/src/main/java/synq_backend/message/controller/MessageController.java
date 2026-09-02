package synq_backend.message.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import synq_backend.message.dto.MessageDTO;
import synq_backend.message.dto.SendMessageRequest;
import synq_backend.message.service.MessageService;
import synq_backend.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService){
        this.messageService =messageService;
    }

    // Sends a message from the authenticated user.
    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(
            Authentication authentication,
            @RequestBody SendMessageRequest request
    ){
        User user = (User) authentication.getPrincipal();

        MessageDTO message = messageService.sendMessage(
                user.getId(),
                request
        );

        return ResponseEntity.ok(message);
    }

    // Retrieves all messages from a conversation for the authenticated user.
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessages(
            Authentication authentication,
            @PathVariable UUID conversationId
    ){
        User user = (User) authentication.getPrincipal();

        List<MessageDTO> messages = messageService.getMessages(
                user.getId(),
                conversationId
        );

        return ResponseEntity.ok(messages);
    }

    // Soft deletes a message for the authenticated sender.
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            Authentication authentication,
            @PathVariable UUID messageId
    ){
        User user = (User) authentication.getPrincipal();

        messageService.deleteMessage(
                user.getId(),
                messageId
        );

        return ResponseEntity.noContent().build();
    }
}
