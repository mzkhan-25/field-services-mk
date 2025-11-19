package com.fieldservices.repository;

import com.fieldservices.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific task
     */
    List<Notification> findByTaskId(Long taskId);

    /**
     * Find all notifications for a specific customer
     */
    List<Notification> findByCustomerId(Long customerId);

    /**
     * Find notifications by delivery status
     */
    List<Notification> findByDeliveryStatus(Notification.DeliveryStatus status);
}
