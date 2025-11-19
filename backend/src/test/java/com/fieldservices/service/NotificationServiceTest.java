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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;
    private NotificationRequest testRequest;
    private Task testTask;
    private User testTechnician;

    @BeforeEach
    void setUp() {
        testTechnician = new User();
        testTechnician.setId(1L);
        testTechnician.setUsername("tech1");
        testTechnician.setEmail("tech1@example.com");
        testTechnician.setRole(User.Role.TECHNICIAN);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setClientAddress("123 Main St");
        testTask.setPriority(Task.Priority.HIGH);
        testTask.setEstimatedDuration(60);
        testTask.setStatus(Task.TaskStatus.ASSIGNED);
        testTask.setAssignedTechnician(testTechnician);
        testTask.setAssignedAt(LocalDateTime.now());

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTaskId(1L);
        testNotification.setCustomerId(1L);
        testNotification.setType(Notification.NotificationType.TASK_ASSIGNED);
        testNotification.setMessage("Test notification message");
        testNotification.setChannel(Notification.NotificationChannel.EMAIL);
        testNotification.setRecipientContact("customer@example.com");
        testNotification.setDeliveryStatus(Notification.DeliveryStatus.PENDING);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setUpdatedAt(LocalDateTime.now());

        testRequest = new NotificationRequest();
        testRequest.setTaskId(1L);
        testRequest.setCustomerId(1L);
        testRequest.setType(Notification.NotificationType.TASK_ASSIGNED);
        testRequest.setMessage("Test notification message");
        testRequest.setChannel(Notification.NotificationChannel.EMAIL);
        testRequest.setRecipientContact("customer@example.com");
    }

    @Test
    void sendNotification_Success_Email() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        NotificationResponse response = notificationService.sendNotification(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testNotification.getId(), response.getId());
        assertEquals(testNotification.getTaskId(), response.getTaskId());
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotification_Success_SMS() {
        // Arrange
        testRequest.setChannel(Notification.NotificationChannel.SMS);
        testNotification.setChannel(Notification.NotificationChannel.SMS);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        NotificationResponse response = notificationService.sendNotification(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testNotification.getId(), response.getId());
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotification_Success_Both() {
        // Arrange
        testRequest.setChannel(Notification.NotificationChannel.BOTH);
        testNotification.setChannel(Notification.NotificationChannel.BOTH);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        NotificationResponse response = notificationService.sendNotification(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testNotification.getId(), response.getId());
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotification_DefaultChannelToEmail() {
        // Arrange
        testRequest.setChannel(null);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        NotificationResponse response = notificationService.sendNotification(testRequest);

        // Assert
        assertNotNull(response);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotification_Failure_EmailException() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doThrow(new RuntimeException("Email service unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        NotificationResponse response = notificationService.sendNotification(testRequest);

        // Assert
        assertNotNull(response);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendTaskAssignmentNotification_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.sendTaskAssignmentNotification(1L, 1L, "customer@example.com");

        // Assert
        verify(taskRepository, times(1)).findById(1L);
        // Notification is saved twice: once when created, once when marked as sent
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendTaskAssignmentNotification_TaskNotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            notificationService.sendTaskAssignmentNotification(1L, 1L, "customer@example.com")
        );
        verify(taskRepository, times(1)).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void sendTaskInProgressNotification_Success() {
        // Arrange
        testTask.setStatus(Task.TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.sendTaskInProgressNotification(1L, 1L, "customer@example.com");

        // Assert
        verify(taskRepository, times(1)).findById(1L);
        // Notification is saved twice: once when created, once when marked as sent
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendTaskInProgressNotification_TaskNotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            notificationService.sendTaskInProgressNotification(1L, 1L, "customer@example.com")
        );
        verify(taskRepository, times(1)).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getNotificationsByTaskId_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByTaskId(1L)).thenReturn(notifications);

        // Act
        List<NotificationResponse> responses = notificationService.getNotificationsByTaskId(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testNotification.getId(), responses.get(0).getId());
        verify(notificationRepository, times(1)).findByTaskId(1L);
    }

    @Test
    void getNotificationsByTaskId_EmptyList() {
        // Arrange
        when(notificationRepository.findByTaskId(1L)).thenReturn(Arrays.asList());

        // Act
        List<NotificationResponse> responses = notificationService.getNotificationsByTaskId(1L);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(notificationRepository, times(1)).findByTaskId(1L);
    }

    @Test
    void retryFailedNotifications_Success() {
        // Arrange
        Notification failedNotification = new Notification();
        failedNotification.setId(2L);
        failedNotification.setTaskId(1L);
        failedNotification.setCustomerId(1L);
        failedNotification.setType(Notification.NotificationType.TASK_ASSIGNED);
        failedNotification.setMessage("Test message");
        failedNotification.setChannel(Notification.NotificationChannel.EMAIL);
        failedNotification.setRecipientContact("customer@example.com");
        failedNotification.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        failedNotification.setRetryCount(0);

        when(notificationRepository.findByDeliveryStatus(Notification.DeliveryStatus.FAILED))
                .thenReturn(Arrays.asList(failedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(failedNotification);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.retryFailedNotifications();

        // Assert
        verify(notificationRepository, times(1))
                .findByDeliveryStatus(Notification.DeliveryStatus.FAILED);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void retryFailedNotifications_MaxRetryReached() {
        // Arrange
        Notification failedNotification = new Notification();
        failedNotification.setId(2L);
        failedNotification.setTaskId(1L);
        failedNotification.setCustomerId(1L);
        failedNotification.setType(Notification.NotificationType.TASK_ASSIGNED);
        failedNotification.setMessage("Test message");
        failedNotification.setChannel(Notification.NotificationChannel.EMAIL);
        failedNotification.setRecipientContact("customer@example.com");
        failedNotification.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        failedNotification.setRetryCount(3); // Max retries reached

        when(notificationRepository.findByDeliveryStatus(Notification.DeliveryStatus.FAILED))
                .thenReturn(Arrays.asList(failedNotification));

        // Act
        notificationService.retryFailedNotifications();

        // Assert
        verify(notificationRepository, times(1))
                .findByDeliveryStatus(Notification.DeliveryStatus.FAILED);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void retryFailedNotifications_NoFailedNotifications() {
        // Arrange
        when(notificationRepository.findByDeliveryStatus(Notification.DeliveryStatus.FAILED))
                .thenReturn(Arrays.asList());

        // Act
        notificationService.retryFailedNotifications();

        // Assert
        verify(notificationRepository, times(1))
                .findByDeliveryStatus(Notification.DeliveryStatus.FAILED);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
