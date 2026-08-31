package synq_backend.conversation.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import synq_backend.conversation.dto.ConversationDTO;
import synq_backend.conversation.entity.Conversation;
import synq_backend.conversation.entity.ConversationParticipant;
import synq_backend.conversation.entity.ConversationType;
import synq_backend.conversation.repository.ConversationParticipantRepository;
import synq_backend.conversation.repository.ConversationRepository;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;

import java.util.UUID;

// Contains the business logic for creating and managing conversations.
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserRepository userRepository
    ){
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }


    // Creates a direct conversation between the current user and another user.
    @Transactional
    public ConversationDTO createDirectConversation(
            UUID currentUserId,
            UUID otherUserId
    ) {

        // Prevents a user from creating a conversation with themselves.
        if (currentUserId.equals(otherUserId)) {
            throw new IllegalArgumentException(
                    "A user cannot create a conversation with themselves"
            );
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Current user not found")
                );

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        // Returns the existing conversation if these users already have one.
        Conversation conversation = participantRepository
                .findDirectConversation(currentUserId, otherUserId)
                .orElseGet(() -> {

                    // Creates a new direct conversation when none exists.
                    Conversation newConversation =
                            new Conversation(ConversationType.DIRECT, currentUser);

                    newConversation = conversationRepository.save(newConversation);

                    ConversationParticipant currentParticipant =
                            new ConversationParticipant(
                                    newConversation,
                                    currentUser
                            );

                    ConversationParticipant otherParticipant =
                            new ConversationParticipant(
                                    newConversation,
                                    otherUser
                            );

                    participantRepository.save(currentParticipant);
                    participantRepository.save(otherParticipant);

                    return newConversation;
                });

        return new ConversationDTO(
                conversation.getId(),
                conversation.getType().name(),
                conversation.getCreatedBy().getId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
