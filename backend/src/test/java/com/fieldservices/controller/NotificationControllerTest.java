package com.fieldservices.controller;

import com.fieldservices.dto.NotificationRequest;
import com.fieldservices.dto.NotificationResponse;
import com.fieldservices.model.Notification;
import com.fieldservices.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationRequest testRequest;
    private NotificationResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new NotificationRequest();
        testRequest.setTaskId(1L);
        testRequest.setCustomerId(1L);
        testRequest.setType(Notification.NotificationType.TASK_ASSIGNED);
        testRequest.setMessage("Test notification message");
        testRequest.setChannel(Notification.NotificationChannel.EMAIL);
        testRequest.setRecipientContact("customer@example.com");

        testResponse = NotificationResponse.builder()
                .id(1L)
                .taskId(1L)
                .customerId(1L)
                .type(Notification.NotificationType.TASK_ASSIGNED)
                .message("Test notification message")
                .channel(Notification.NotificationChannel.EMAIL)
                .recipientContact("customer@example.com")
                .deliveryStatus(Notification.DeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void sendNotification_Success() {
        // Arrange
        when(notificationService.sendNotification(any(NotificationRequest.class)))
                .thenReturn(testResponse);

        // Act
        ResponseEntity<NotificationResponse> response = 
                notificationController.sendNotification(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(1L, response.getBody().getTaskId());
        assertEquals(1L, response.getBody().getCustomerId());
        assertEquals(Notification.DeliveryStatus.SENT, response.getBody().getDeliveryStatus());
        verify(notificationService, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void sendNotification_NullChannel_DefaultsToEmail() {
        // Arrange
        testRequest.setChannel(null);
        when(notificationService.sendNotification(any(NotificationRequest.class)))
                .thenReturn(testResponse);

        // Act
        ResponseEntity<NotificationResponse> response = 
                notificationController.sendNotification(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(notificationService, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void getNotificationsByTaskId_Success() {
        // Arrange
        List<NotificationResponse> notifications = Arrays.asList(testResponse);
        when(notificationService.getNotificationsByTaskId(1L)).thenReturn(notifications);

        // Act
        ResponseEntity<List<NotificationResponse>> response = 
                notificationController.getNotificationsByTaskId(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        verify(notificationService, times(1)).getNotificationsByTaskId(1L);
    }

    @Test
    void getNotificationsByTaskId_EmptyList() {
        // Arrange
        when(notificationService.getNotificationsByTaskId(1L)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<NotificationResponse>> response = 
                notificationController.getNotificationsByTaskId(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(notificationService, times(1)).getNotificationsByTaskId(1L);
    }

    @Test
    void getNotificationsByTaskId_MultipleNotifications() {
        // Arrange
        NotificationResponse response2 = NotificationResponse.builder()
                .id(2L)
                .taskId(1L)
                .customerId(1L)
                .type(Notification.NotificationType.TASK_IN_PROGRESS)
                .message("Task in progress")
                .channel(Notification.NotificationChannel.EMAIL)
                .recipientContact("customer@example.com")
                .deliveryStatus(Notification.DeliveryStatus.SENT)
                .sentAt(LocalDateTime.now())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<NotificationResponse> notifications = Arrays.asList(testResponse, response2);
        when(notificationService.getNotificationsByTaskId(1L)).thenReturn(notifications);

        // Act
        ResponseEntity<List<NotificationResponse>> response = 
                notificationController.getNotificationsByTaskId(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(notificationService, times(1)).getNotificationsByTaskId(1L);
    }

    @Test
    void retryFailedNotifications_Success() {
        // Arrange
        doNothing().when(notificationService).retryFailedNotifications();

        // Act
        ResponseEntity<Void> response = notificationController.retryFailedNotifications();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).retryFailedNotifications();
    }

    @Test
    void sendNotification_ServiceThrowsException() {
        // Arrange
        when(notificationService.sendNotification(any(NotificationRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                notificationController.sendNotification(testRequest)
        );
        verify(notificationService, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void getNotificationsByTaskId_ServiceThrowsException() {
        // Arrange
        when(notificationService.getNotificationsByTaskId(1L))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                notificationController.getNotificationsByTaskId(1L)
        );
        verify(notificationService, times(1)).getNotificationsByTaskId(1L);
    }
}

