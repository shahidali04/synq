package synq_backend.message.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import synq_backend.message.dto.EditMessageRequest;
import synq_backend.message.dto.MessageDTO;
import synq_backend.message.dto.SendMessageRequest;
import synq_backend.message.service.MessageService;
import synq_backend.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

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

    // Retrieves paginated messages from a conversation.
    @GetMapping("/conversation/{conversationId}")
    public List<MessageDTO> getMessages(
            Authentication authentication,
            @PathVariable UUID conversationId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();

        return messageService.getMessages(
                user.getId(),
                conversationId,
                pageable
        );
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

    // Updates an existing message for the authenticated sender.
    @PatchMapping("/{messageId}")
    public MessageDTO editMessage(
            Authentication authentication,
            @PathVariable UUID messageId,
            @Valid @RequestBody EditMessageRequest request
    ){
        User user = (User) authentication.getPrincipal();

        return messageService.editMessage(
                user.getId(),
                messageId,
                request
        );
    }
}
