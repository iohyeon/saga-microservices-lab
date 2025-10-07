package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendNotification(NotificationRequest request) {
        log.info("=================================================");
        log.info("📢 NOTIFICATION");
        log.info("Request ID: {}", request.getRequestId());
        log.info("Account ID: {}", request.getAccountId());
        log.info("Type: {}", request.getType());
        log.info("Message: {}", request.getMessage());
        log.info("=================================================");

        // 실제로는 여기서 SMS, 이메일, 푸시 알림 등 발송
        // 학습용이므로 로그만 출력
    }
}
