package synq_backend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import synq_backend.notification.entity.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Finds all notifications belonging to a specific user.
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
