package synq_backend.message.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import synq_backend.message.entity.Message;

import java.util.UUID;

// Provides database operations for Message entities.
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Retrieves paginated messages from a conversation ordered by creation time.
    Page<Message> findByConversationIdOrderByCreatedAtAsc(
            UUID conversationId,
            Pageable pageable
    );
}
