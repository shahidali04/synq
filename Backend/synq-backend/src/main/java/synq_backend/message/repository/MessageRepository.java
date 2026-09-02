package synq_backend.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import synq_backend.message.entity.Message;

import java.util.List;
import java.util.UUID;

// Provides database operations for Message entities.
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Retrieves messages from a conversation ordered by creation time.
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
