package synq_backend.message.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import synq_backend.conversation.entity.Conversation;
import synq_backend.conversation.repository.ConversationParticipantRepository;
import synq_backend.conversation.repository.ConversationRepository;
import synq_backend.message.dto.MessageDTO;
import synq_backend.message.dto.SendMessageRequest;
import synq_backend.message.entity.Message;
import synq_backend.message.repository.MessageRepository;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserRepository userRepository
    ){
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    // Sends a message from the authenticated user inside a conversation.
    @Transactional
    public MessageDTO sendMessage(
            UUID currentId,
            SendMessageRequest request
    ){
        User sender = userRepository.findById(currentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Conversation conversation = conversationRepository.findById(
                request.getConversationId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Conversation not found"));

        // Ensures that only conversation participants can send messages.
        boolean isParticipant = participantRepository.existsByConversationIdAndUserId(
                conversation.getId(),
                currentId
        );

        if (!isParticipant){
            throw new IllegalArgumentException(
                    "User is not a Participant of this conversation"
            );
        }

        Message message = new Message(
                conversation,
                sender,
                request.getContent()
        );

        Message savedMessage = messageRepository.save(message);

        return new MessageDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt()
        );
    }

    // Retrieves all messages from a conversation for the authenticated user.
    @Transactional
    public List<MessageDTO> getMessages(
            UUID currentId,
            UUID conversationId
    ){
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation not found"));

        // Ensures that only conversation participants can view messages.
        boolean isParticipant = participantRepository.existsByConversationIdAndUserId(
                conversation.getId(),
                currentId
        );

        if (!isParticipant){
            throw new IllegalArgumentException("User is not a participant of this Conversation");
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                conversationId
        );

        return  messages.stream()
                .map(message -> new MessageDTO(
                        message.getId(),
                        message.getConversation().getId(),
                        message.getSender().getId(),
                        message.getContent(),
                        message.getCreatedAt(),
                        message.getUpdatedAt()
                ))
                .toList();
    }
}
