package synq_backend.notification.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import synq_backend.notification.dto.NotificationDTO;
import synq_backend.notification.entity.Notification;
import synq_backend.notification.entity.NotificationType;
import synq_backend.notification.repository.NotificationRepository;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ){
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Creates and stores a notification for a user.
    @Transactional
    public NotificationDTO createNotification(
            UUID userId,
            NotificationType type,
            String message,
            UUID referenceId
    ){
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Notification notification = new Notification(
                user,
                type,
                message,
                referenceId
        );

        Notification savedNotification = notificationRepository.save(notification);

        NotificationDTO notificationDTO = toDTO(savedNotification);

        // Send the notification to the specific user in real time.
        messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/notifications",
                notificationDTO
        );

        return notificationDTO;
    }

    // Returns all notifications belonging to a user.
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(UUID userId){

        return  notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Marks a notification as read.
    @Transactional
    public NotificationDTO markAsRead(
            UUID notificationId,
            UUID userId
    ){
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notification not found"));


        if (!notification.getUser().getId().equals(userId)){
            throw  new IllegalArgumentException(
                    "User is not authorized to modify this notification");
        }

        notification.setRead(true);

        return toDTO(notification);
    }

    // Converts a Notification entity into a NotificationDTO.
    private NotificationDTO toDTO(Notification notification) {

        return new NotificationDTO(
                notification.getId(),
                notification.getUser().getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}


