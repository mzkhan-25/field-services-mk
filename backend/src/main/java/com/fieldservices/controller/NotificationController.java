package com.fieldservices.controller;

import com.fieldservices.dto.NotificationRequest;
import com.fieldservices.dto.NotificationResponse;
import com.fieldservices.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Notification Management
 * 
 * Endpoints:
 * - POST /api/notifications/send - Send a notification (internal use, DISPATCHER, SUPERVISOR)
 * - GET /api/notifications/{taskId} - Get notifications for a task (all authenticated users)
 * - POST /api/notifications/retry - Retry failed notifications (SUPERVISOR)
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send a notification
     * Accessible by: DISPATCHER, SUPERVISOR
     * This is primarily for internal use or manual triggering
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'SUPERVISOR')")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        log.info("Received request to send notification for task: {}", request.getTaskId());
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all notifications for a task
     * Accessible by: all authenticated users
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByTaskId(
            @PathVariable Long taskId) {
        log.info("Received request to get notifications for task: {}", taskId);
        List<NotificationResponse> notifications = notificationService.getNotificationsByTaskId(taskId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Retry failed notifications
     * Accessible by: SUPERVISOR
     */
    @PostMapping("/retry")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Void> retryFailedNotifications() {
        log.info("Received request to retry failed notifications");
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok().build();
    }
}
