package com.fieldservices.dto;

import com.fieldservices.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long taskId;
    private Long customerId;
    private Notification.NotificationType type;
    private String message;
    private LocalDateTime sentAt;
    private Notification.DeliveryStatus deliveryStatus;
    private Notification.NotificationChannel channel;
    private String recipientContact;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .taskId(notification.getTaskId())
                .customerId(notification.getCustomerId())
                .type(notification.getType())
                .message(notification.getMessage())
                .sentAt(notification.getSentAt())
                .deliveryStatus(notification.getDeliveryStatus())
                .channel(notification.getChannel())
                .recipientContact(notification.getRecipientContact())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
