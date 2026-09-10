package synq_backend.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import synq_backend.notification.dto.NotificationDTO;
import synq_backend.notification.service.NotificationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ){
        this.notificationService = notificationService;
    }

    // Returns all notifications belonging to the authenticated user.
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @RequestParam UUID currentUserId
    ){
        return ResponseEntity.ok(
                notificationService.getNotifications(currentUserId)
        );
    }

    // Marks a notification as read.
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable UUID notificationId,
            @RequestParam UUID currentUserId
    ){
        return ResponseEntity.ok(
                notificationService.markAsRead(
                        notificationId,
                        currentUserId
                )
        );
    }
}
