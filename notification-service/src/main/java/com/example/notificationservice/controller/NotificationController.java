package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationRequest;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendNotification(@RequestBody NotificationRequest request) {
        log.info("Notification request: {}", request);
        notificationService.sendNotification(request);
        return ResponseEntity.ok(Map.of("status", "sent"));
    }
}
