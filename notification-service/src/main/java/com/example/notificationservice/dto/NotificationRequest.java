package com.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String requestId;
    private String accountId;
    private String message;
    private NotificationType type;

    public enum NotificationType {
        TRANSFER_SUCCESS,
        TRANSFER_FAILED
    }
}
