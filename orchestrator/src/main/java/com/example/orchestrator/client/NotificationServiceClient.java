package com.example.orchestrator.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class NotificationServiceClient {

    private final RestClient restClient;
    private final String notificationServiceUrl;

    public NotificationServiceClient(RestClient.Builder restClientBuilder,
                                     @Value("${services.notification.url}") String notificationServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public void sendNotification(String requestId, String accountId, String type, String message) {
        log.info("Calling Notification Service: requestId={}, accountId={}, type={}", requestId, accountId, type);

        restClient.post()
                .uri(notificationServiceUrl + "/notifications/send")
                .body(Map.of(
                        "requestId", requestId,
                        "accountId", accountId,
                        "type", type,
                        "message", message
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, res) -> {
                    throw new RuntimeException("Notification failed: " + res.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, res) -> {
                    throw new RuntimeException("Notification service error: " + res.getStatusText());
                })
                .toBodilessEntity();

        log.info("Notification sent successfully");
    }
}
