package synq_backend.message.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import synq_backend.common.exception.InvalidMessageOperationException;
import synq_backend.common.exception.ResourceNotFoundException;
import synq_backend.conversation.entity.Conversation;
import synq_backend.conversation.entity.ConversationParticipant;
import synq_backend.conversation.repository.ConversationParticipantRepository;
import synq_backend.conversation.repository.ConversationRepository;
import synq_backend.message.dto.MessageDTO;
import synq_backend.message.dto.SendMessageRequest;
import synq_backend.message.entity.Message;
import synq_backend.message.repository.MessageRepository;
import synq_backend.notification.entity.NotificationType;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;
import synq_backend.message.dto.EditMessageRequest;
import synq_backend.notification.service.NotificationService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ){
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

        // Detect @username mentions in the message content.
        Pattern mentionPattern = Pattern.compile("@([a-zA-Z0-9_]+)");
        Matcher matcher = mentionPattern.matcher(request.getContent());

        while (matcher.find()) {

            String username = matcher.group(1);

            userRepository.findByUsername(username)
                    .ifPresent(user -> {

                        boolean isMentionedUserParticipant =
                                participantRepository.existsByConversationIdAndUserId(
                                                conversation.getId(),
                                                user.getId()
                                );

                        if (isMentionedUserParticipant) {

                            System.out.println(
                                    "Valid mention: @" + user.getUsername()
                                    );

                        } else {
                            System.out.println(
                                    "Ignored mention: @" + user.getUsername()
                                            + " is not a participant"
                            );
                        }

                    });
        }

        Message message = new Message(
                conversation,
                sender,
                request.getContent()
        );

        Message savedMessage = messageRepository.save(message);

        // Find all participants in this conversation.
        List<ConversationParticipant> participants =
                participantRepository.findByConversationId(
                        conversation.getId()
                );

        // Create a notification for every participant except the sender.
        for (ConversationParticipant participant : participants) {

            UUID recipientId = participant.getUser().getId();

            if (!recipientId.equals(currentId)) {

                notificationService.createNotification(
                        recipientId,
                        NotificationType.MESSAGE,
                        "You have a new message",
                        conversation.getId()
                );
            }
        }

        return new MessageDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt(),
                savedMessage.getDeliveredAt(),
                savedMessage.getReadAt(),
                savedMessage.getDeletedAt() != null,
                savedMessage.getType()
        );
    }

    // Retrieves all messages from a conversation for the authenticated user.
    @Transactional
    public List<MessageDTO> getMessages(
            UUID currentId,
            UUID conversationId,
            Pageable pageable
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

        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                conversationId,
                pageable
        );

        return  messages.stream()
                .map(message -> new MessageDTO(
                        message.getId(),
                        message.getConversation().getId(),
                        message.getSender().getId(),
                        message.getDeletedAt() != null
                                ? null
                                : message.getContent(),
                        message.getCreatedAt(),
                        message.getUpdatedAt(),
                        message.getDeliveredAt(),
                        message.getReadAt(),
                        message.getDeletedAt() != null,
                        message.getType()
                ))
                .toList();
    }

    // Soft deletes a message when requested by its sender.
    @Transactional
    public void deleteMessage(UUID currentId, UUID messageId){

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Message not found"));

        if (!message.getSender().getId().equals(currentId)){
            throw new IllegalArgumentException("User is not the sender of this message");
        }

        message.setDeletedAt(OffsetDateTime.now());

        messageRepository.save(message);
    }

    // Updates the content of an existing message for the authenticated sender.
    @Transactional
    public MessageDTO editMessage(UUID currentUserId,
                                  UUID messageId,
                                  EditMessageRequest request) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Message not found")
                );

        // Only the sender can edit the message.
        if (!message.getSender().getId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "User is not the sender of this message"
            );
        }

        // Deleted messages cannot be edited.
        if (message.getDeletedAt() != null) {
            throw new InvalidMessageOperationException(
                    "Deleted messages cannot be edited"
            );
        }

        message.setContent(request.getContent());

        Message savedMessage = messageRepository.save(message);

        return new MessageDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt(),
                savedMessage.getDeliveredAt(),
                savedMessage.getReadAt(),
                savedMessage.getDeletedAt() != null,
                savedMessage.getType()
        );
    }

    // Marks a message as delivered.
    @Transactional
    public MessageDTO markAsDelivered(UUID currentUserId, UUID messageId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Message not found")
                );

        // Only a recipient can mark a message as delivered.
        if (message.getSender().getId().equals(currentUserId)) {
            throw new InvalidMessageOperationException(
                    "Sender cannot mark their own message as delivered"
            );
        }

        // Set delivery timestamp only once.
        if (message.getDeliveredAt() == null) {
            message.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return new MessageDTO(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getDeletedAt() != null
                        ? null
                        : message.getContent(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getDeliveredAt(),
                message.getReadAt(),
                message.getDeletedAt() != null,
                message.getType()
        );
    }

    // Marks a message as read by the recipient.
    @Transactional
    public MessageDTO markAsRead(UUID currentUserId, UUID messageId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Message not found")
                );

        // Only the recipient can mark the message as read.
        if (message.getSender().getId().equals(currentUserId)) {
            throw new InvalidMessageOperationException(
                    "Sender cannot mark their own message as read"
            );
        }

        // Set read timestamp only once.
        if (message.getReadAt() == null) {
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return new MessageDTO(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getDeletedAt() != null
                        ? null
                        : message.getContent(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getDeliveredAt(),
                message.getReadAt(),
                message.getDeletedAt() != null,
                message.getType()
        );
    }
}
