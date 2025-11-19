package com.fieldservices.dto;

import com.fieldservices.model.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Notification type is required")
    private Notification.NotificationType type;

    @NotBlank(message = "Message is required")
    private String message;

    private Notification.NotificationChannel channel;

    private String recipientContact;
}
