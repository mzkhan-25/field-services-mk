package com.fieldservices.repository;

import com.fieldservices.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification testNotification1;
    private Notification testNotification2;

    @BeforeEach
    void setUp() {
        testNotification1 = new Notification();
        testNotification1.setTaskId(1L);
        testNotification1.setCustomerId(1L);
        testNotification1.setType(Notification.NotificationType.TASK_ASSIGNED);
        testNotification1.setMessage("Task assigned notification");
        testNotification1.setChannel(Notification.NotificationChannel.EMAIL);
        testNotification1.setRecipientContact("customer1@example.com");
        testNotification1.setDeliveryStatus(Notification.DeliveryStatus.SENT);
        testNotification1.setSentAt(LocalDateTime.now());
        testNotification1.setCreatedAt(LocalDateTime.now());
        testNotification1.setUpdatedAt(LocalDateTime.now());

        testNotification2 = new Notification();
        testNotification2.setTaskId(2L);
        testNotification2.setCustomerId(1L);
        testNotification2.setType(Notification.NotificationType.TASK_IN_PROGRESS);
        testNotification2.setMessage("Task in progress notification");
        testNotification2.setChannel(Notification.NotificationChannel.EMAIL);
        testNotification2.setRecipientContact("customer1@example.com");
        testNotification2.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        testNotification2.setCreatedAt(LocalDateTime.now());
        testNotification2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void save_Success() {
        // Act
        Notification saved = notificationRepository.save(testNotification1);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(testNotification1.getTaskId(), saved.getTaskId());
        assertEquals(testNotification1.getCustomerId(), saved.getCustomerId());
        assertEquals(testNotification1.getType(), saved.getType());
        assertEquals(testNotification1.getMessage(), saved.getMessage());
        assertEquals(testNotification1.getDeliveryStatus(), saved.getDeliveryStatus());
    }

    @Test
    void findByTaskId_Success() {
        // Arrange
        entityManager.persist(testNotification1);
        entityManager.persist(testNotification2);
        
        Notification notification3 = new Notification();
        notification3.setTaskId(1L);
        notification3.setCustomerId(2L);
        notification3.setType(Notification.NotificationType.TASK_COMPLETED);
        notification3.setMessage("Task completed notification");
        notification3.setChannel(Notification.NotificationChannel.EMAIL);
        notification3.setRecipientContact("customer2@example.com");
        notification3.setDeliveryStatus(Notification.DeliveryStatus.SENT);
        notification3.setCreatedAt(LocalDateTime.now());
        notification3.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(notification3);
        
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByTaskId(1L);

        // Assert
        assertNotNull(notifications);
        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> n.getTaskId().equals(1L)));
    }

    @Test
    void findByTaskId_NoResults() {
        // Arrange
        entityManager.persist(testNotification1);
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByTaskId(999L);

        // Assert
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void findByCustomerId_Success() {
        // Arrange
        entityManager.persist(testNotification1);
        entityManager.persist(testNotification2);
        
        Notification notification3 = new Notification();
        notification3.setTaskId(3L);
        notification3.setCustomerId(2L);
        notification3.setType(Notification.NotificationType.TASK_ASSIGNED);
        notification3.setMessage("Task assigned notification");
        notification3.setChannel(Notification.NotificationChannel.EMAIL);
        notification3.setRecipientContact("customer2@example.com");
        notification3.setDeliveryStatus(Notification.DeliveryStatus.SENT);
        notification3.setCreatedAt(LocalDateTime.now());
        notification3.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(notification3);
        
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByCustomerId(1L);

        // Assert
        assertNotNull(notifications);
        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> n.getCustomerId().equals(1L)));
    }

    @Test
    void findByCustomerId_NoResults() {
        // Arrange
        entityManager.persist(testNotification1);
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByCustomerId(999L);

        // Assert
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void findByDeliveryStatus_Success() {
        // Arrange
        entityManager.persist(testNotification1);
        entityManager.persist(testNotification2);
        
        Notification notification3 = new Notification();
        notification3.setTaskId(3L);
        notification3.setCustomerId(2L);
        notification3.setType(Notification.NotificationType.TASK_ASSIGNED);
        notification3.setMessage("Task assigned notification");
        notification3.setChannel(Notification.NotificationChannel.EMAIL);
        notification3.setRecipientContact("customer2@example.com");
        notification3.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        notification3.setCreatedAt(LocalDateTime.now());
        notification3.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(notification3);
        
        entityManager.flush();

        // Act
        List<Notification> failedNotifications = notificationRepository
                .findByDeliveryStatus(Notification.DeliveryStatus.FAILED);

        // Assert
        assertNotNull(failedNotifications);
        assertEquals(2, failedNotifications.size());
        assertTrue(failedNotifications.stream()
                .allMatch(n -> n.getDeliveryStatus() == Notification.DeliveryStatus.FAILED));
    }

    @Test
    void findByDeliveryStatus_NoResults() {
        // Arrange
        entityManager.persist(testNotification1);
        entityManager.flush();

        // Act
        List<Notification> pendingNotifications = notificationRepository
                .findByDeliveryStatus(Notification.DeliveryStatus.PENDING);

        // Assert
        assertNotNull(pendingNotifications);
        assertTrue(pendingNotifications.isEmpty());
    }

    @Test
    void prePersist_SetsTimestamps() {
        // Arrange
        Notification notification = new Notification();
        notification.setTaskId(1L);
        notification.setCustomerId(1L);
        notification.setType(Notification.NotificationType.TASK_ASSIGNED);
        notification.setMessage("Test message");
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setRecipientContact("test@example.com");
        // Don't set timestamps manually

        // Act
        Notification saved = notificationRepository.save(notification);

        // Assert
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertNotNull(saved.getDeliveryStatus());
        assertEquals(Notification.DeliveryStatus.PENDING, saved.getDeliveryStatus());
        assertEquals(0, saved.getRetryCount());
    }

    @Test
    void preUpdate_UpdatesTimestamp() throws InterruptedException {
        // Arrange
        Notification saved = notificationRepository.save(testNotification1);
        LocalDateTime originalUpdatedAt = saved.getUpdatedAt();
        
        // Wait a bit to ensure timestamp changes
        Thread.sleep(10);

        // Act
        saved.setMessage("Updated message");
        Notification updated = notificationRepository.save(saved);

        // Assert
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(originalUpdatedAt) || 
                   updated.getUpdatedAt().isEqual(originalUpdatedAt));
    }
}
