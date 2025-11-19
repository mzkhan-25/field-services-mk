package com.fieldservices.service;

import com.fieldservices.dto.NotificationRequest;
import com.fieldservices.dto.NotificationResponse;
import com.fieldservices.model.Notification;
import com.fieldservices.model.Task;
import com.fieldservices.model.User;
import com.fieldservices.repository.NotificationRepository;
import com.fieldservices.repository.TaskRepository;
import com.fieldservices.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private static final int MAX_RETRY_COUNT = 3;
    private static final String FROM_EMAIL = "noreply@fieldservices.com";

    /**
     * Send a notification asynchronously
     */
    @Async
    @Transactional
    public void sendNotificationAsync(NotificationRequest request) {
        log.info("Sending notification asynchronously for task: {}", request.getTaskId());
        sendNotification(request);
    }

    /**
     * Send a notification
     */
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Sending notification for task: {} to customer: {}", 
                request.getTaskId(), request.getCustomerId());

        // Create notification record
        Notification notification = new Notification();
        notification.setTaskId(request.getTaskId());
        notification.setCustomerId(request.getCustomerId());
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setChannel(request.getChannel() != null ? 
                request.getChannel() : Notification.NotificationChannel.EMAIL);
        notification.setRecipientContact(request.getRecipientContact());
        notification.setDeliveryStatus(Notification.DeliveryStatus.PENDING);

        Notification savedNotification = notificationRepository.save(notification);

        // Send the notification via appropriate channel
        try {
            if (notification.getChannel() == Notification.NotificationChannel.EMAIL || 
                notification.getChannel() == Notification.NotificationChannel.BOTH) {
                sendEmailNotification(savedNotification);
            }
            
            if (notification.getChannel() == Notification.NotificationChannel.SMS || 
                notification.getChannel() == Notification.NotificationChannel.BOTH) {
                sendSmsNotification(savedNotification);
            }

            savedNotification.setDeliveryStatus(Notification.DeliveryStatus.SENT);
            savedNotification.setSentAt(LocalDateTime.now());
            notificationRepository.save(savedNotification);
            
            log.info("Notification sent successfully for task: {}", request.getTaskId());
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            savedNotification.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
            savedNotification.setErrorMessage(e.getMessage());
            notificationRepository.save(savedNotification);
        }

        return NotificationResponse.fromEntity(savedNotification);
    }

    /**
     * Send task assignment notification
     */
    @Transactional
    public void sendTaskAssignmentNotification(Long taskId, Long customerId, String customerEmail) {
        log.info("Preparing task assignment notification for task: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        String message = buildTaskAssignmentMessage(task);

        NotificationRequest request = new NotificationRequest();
        request.setTaskId(taskId);
        request.setCustomerId(customerId);
        request.setType(Notification.NotificationType.TASK_ASSIGNED);
        request.setMessage(message);
        request.setChannel(Notification.NotificationChannel.EMAIL);
        request.setRecipientContact(customerEmail);

        sendNotificationAsync(request);
    }

    /**
     * Send task in-progress notification
     */
    @Transactional
    public void sendTaskInProgressNotification(Long taskId, Long customerId, String customerEmail) {
        log.info("Preparing task in-progress notification for task: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        String message = buildTaskInProgressMessage(task);

        NotificationRequest request = new NotificationRequest();
        request.setTaskId(taskId);
        request.setCustomerId(customerId);
        request.setType(Notification.NotificationType.TASK_IN_PROGRESS);
        request.setMessage(message);
        request.setChannel(Notification.NotificationChannel.EMAIL);
        request.setRecipientContact(customerEmail);

        sendNotificationAsync(request);
    }

    /**
     * Get all notifications for a task
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByTaskId(Long taskId) {
        log.info("Fetching notifications for task: {}", taskId);
        return notificationRepository.findByTaskId(taskId).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retry failed notifications
     */
    @Transactional
    public void retryFailedNotifications() {
        log.info("Retrying failed notifications");
        
        List<Notification> failedNotifications = notificationRepository
                .findByDeliveryStatus(Notification.DeliveryStatus.FAILED);

        for (Notification notification : failedNotifications) {
            if (notification.getRetryCount() < MAX_RETRY_COUNT) {
                log.info("Retrying notification id: {}", notification.getId());
                notification.setRetryCount(notification.getRetryCount() + 1);
                
                NotificationRequest request = new NotificationRequest();
                request.setTaskId(notification.getTaskId());
                request.setCustomerId(notification.getCustomerId());
                request.setType(notification.getType());
                request.setMessage(notification.getMessage());
                request.setChannel(notification.getChannel());
                request.setRecipientContact(notification.getRecipientContact());
                
                sendNotification(request);
            } else {
                log.warn("Max retry count reached for notification id: {}", notification.getId());
            }
        }
    }

    /**
     * Build task assignment message with ETA
     */
    private String buildTaskAssignmentMessage(Task task) {
        StringBuilder message = new StringBuilder();
        message.append("Task Assignment Notification\n\n");
        message.append("Your service request has been assigned to a technician.\n\n");
        message.append("Task Details:\n");
        message.append("- Title: ").append(task.getTitle()).append("\n");
        message.append("- Description: ").append(task.getDescription()).append("\n");
        message.append("- Address: ").append(task.getClientAddress()).append("\n");
        message.append("- Priority: ").append(task.getPriority()).append("\n");
        
        if (task.getAssignedTechnician() != null) {
            message.append("- Technician: ").append(task.getAssignedTechnician().getUsername()).append("\n");
            message.append("- Technician Email: ").append(task.getAssignedTechnician().getEmail()).append("\n");
        }
        
        if (task.getEstimatedDuration() != null) {
            message.append("- Estimated Duration: ").append(task.getEstimatedDuration()).append(" minutes\n");
        }
        
        // Calculate ETA
        String eta = calculateETA(task);
        message.append("- Estimated Time of Arrival: ").append(eta).append("\n");
        
        message.append("\nYou will receive another notification when the technician is on the way.");
        
        return message.toString();
    }

    /**
     * Build task in-progress message with ETA
     */
    private String buildTaskInProgressMessage(Task task) {
        StringBuilder message = new StringBuilder();
        message.append("Task In Progress Notification\n\n");
        message.append("Your technician is now on the way to your location.\n\n");
        message.append("Task Details:\n");
        message.append("- Title: ").append(task.getTitle()).append("\n");
        message.append("- Address: ").append(task.getClientAddress()).append("\n");
        
        if (task.getAssignedTechnician() != null) {
            message.append("- Technician: ").append(task.getAssignedTechnician().getUsername()).append("\n");
            message.append("- Technician Email: ").append(task.getAssignedTechnician().getEmail()).append("\n");
        }
        
        // Calculate updated ETA
        String eta = calculateETA(task);
        message.append("- Estimated Time of Arrival: ").append(eta).append("\n");
        
        message.append("\nPlease be available at the service location.");
        
        return message.toString();
    }

    /**
     * Calculate ETA based on task assignment time and estimated duration
     */
    private String calculateETA(Task task) {
        if (task.getAssignedAt() == null) {
            return "To be determined";
        }

        // Add 30 minutes travel time + estimated duration
        int travelTime = 30; // minutes
        int totalTime = travelTime;
        
        if (task.getEstimatedDuration() != null) {
            totalTime += task.getEstimatedDuration();
        }

        LocalDateTime eta = task.getAssignedAt().plus(totalTime, ChronoUnit.MINUTES);
        
        // Format ETA
        return eta.toLocalDate().toString() + " " + eta.toLocalTime().toString();
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(Notification notification) {
        log.info("Sending email notification id: {}", notification.getId());
        
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(FROM_EMAIL);
            mailMessage.setTo(notification.getRecipientContact());
            mailMessage.setSubject(getEmailSubject(notification.getType()));
            mailMessage.setText(notification.getMessage());
            
            mailSender.send(mailMessage);
            log.info("Email sent successfully for notification id: {}", notification.getId());
        } catch (MailException e) {
            log.error("Failed to send email for notification id: {}", notification.getId(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Send SMS notification (mock implementation)
     */
    private void sendSmsNotification(Notification notification) {
        log.info("Sending SMS notification id: {} (Mock)", notification.getId());
        
        // Mock SMS implementation - in production, integrate with Twilio or similar service
        log.info("SMS would be sent to: {} with message: {}", 
                notification.getRecipientContact(), 
                notification.getMessage());
        
        // Simulate successful SMS sending
        log.info("SMS sent successfully (Mock) for notification id: {}", notification.getId());
    }

    /**
     * Get email subject based on notification type
     */
    private String getEmailSubject(Notification.NotificationType type) {
        return switch (type) {
            case TASK_ASSIGNED -> "Field Service: Task Assigned";
            case TASK_IN_PROGRESS -> "Field Service: Technician On The Way";
            case TASK_COMPLETED -> "Field Service: Task Completed";
            case TASK_CANCELLED -> "Field Service: Task Cancelled";
        };
    }
}
