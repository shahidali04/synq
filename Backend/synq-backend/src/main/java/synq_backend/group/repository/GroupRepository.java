package synq_backend.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import synq_backend.group.entity.Group;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findByConversationId(UUID conversationId);
}
