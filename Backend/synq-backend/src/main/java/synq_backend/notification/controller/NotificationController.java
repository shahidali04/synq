package synq_backend.notification.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import synq_backend.notification.dto.NotificationDTO;
import synq_backend.notification.service.NotificationService;
import synq_backend.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                notificationService.getNotifications(currentUser.getId())
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        notificationId,
                        currentUser.getId()
                )
        );
    }
}