package synq_backend.conversation.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import synq_backend.conversation.dto.ConversationDTO;
import synq_backend.conversation.dto.ConversationRequest;
import synq_backend.conversation.service.ConversationService;
import synq_backend.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService){
        this.conversationService = conversationService;
    }

    // Creates a direct conversation between the current user and another user.
    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestParam UUID currentUserId,
            @RequestBody ConversationRequest request
    ){
        ConversationDTO conversation = conversationService.createDirectConversation(
                currentUserId,
                request.getUserId()
        );

        return ResponseEntity.ok(conversation);
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getUserConversations(
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();

        List<ConversationDTO> conversations = conversationService
                .getUserConversations(user.getId());

        return ResponseEntity.ok(conversations);
    }
}
