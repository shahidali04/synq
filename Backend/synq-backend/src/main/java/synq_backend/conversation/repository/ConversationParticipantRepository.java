package synq_backend.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import synq_backend.conversation.entity.Conversation;
import synq_backend.conversation.entity.ConversationParticipant;
import synq_backend.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Provides database operations for conversation participant records.
public interface ConversationParticipantRepository
extends JpaRepository<ConversationParticipant, Long> {

    // Finds all conversations in which the given user participates.
    List<ConversationParticipant> findByUser(User user);

    // Checks whether a user is already a participant in a specific conversation.
    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    // Finds a participant record for a specific conversation and user.
    Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    // Finds a direct conversation shared by two users, if one already exists.
    @Query("""
    SELECT cp1.conversation
    FROM ConversationParticipant cp1
    JOIN ConversationParticipant cp2
        ON cp1.conversation.id = cp2.conversation.id
    WHERE cp1.user.id = :userId1
      AND cp2.user.id = :userId2
      AND cp1.conversation.type = synq_backend.conversation.entity.ConversationType.DIRECT
    """)
    Optional<Conversation> findDirectConversation(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2
    );
}
