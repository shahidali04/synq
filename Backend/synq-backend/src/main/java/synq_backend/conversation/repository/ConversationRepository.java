package synq_backend.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import synq_backend.conversation.entity.Conversation;

import java.util.UUID;

// Provides database operations for Conversation entities.
public interface ConversationRepository
        extends JpaRepository<Conversation, UUID> {
}
