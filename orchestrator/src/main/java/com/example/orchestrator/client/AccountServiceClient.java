package com.example.orchestrator.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceClient {

    @Value("${account-service.url:http://localhost:8081}")
    private String accountServiceUrl;

    private final RestClient restClient = RestClient.create();

    public void holdFunds(String accountId, String holdId, BigDecimal amount) {
        log.info("Calling account-service: hold funds accountId={}, holdId={}, amount={}",
                accountId, holdId, amount);

        restClient.post()
                .uri(accountServiceUrl + "/accounts/hold")
                .body(Map.of(
                        "accountId", accountId,
                        "holdId", holdId,
                        "amount", amount
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("Hold failed: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Account service error: " + response.getStatusText());
                })
                .toBodilessEntity();

        log.info("Hold successful: holdId={}", holdId);
    }

    public void commitHold(String holdId) {
        log.info("Calling account-service: commit hold holdId={}", holdId);

        restClient.post()
                .uri(accountServiceUrl + "/accounts/commit")
                .body(Map.of("holdId", holdId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("Commit failed: " + response.getStatusText());
                })
                .toBodilessEntity();

        log.info("Commit successful: holdId={}", holdId);
    }

    public void releaseHold(String holdId) {
        log.info("Calling account-service: release hold holdId={}", holdId);

        restClient.post()
                .uri(accountServiceUrl + "/accounts/release")
                .body(Map.of("holdId", holdId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.warn("Release failed: {}", response.getStatusText());
                })
                .toBodilessEntity();

        log.info("Release successful: holdId={}", holdId);
    }

    public void creditAccount(String accountId, BigDecimal amount) {
        log.info("Calling account-service: credit accountId={}, amount={}", accountId, amount);

        restClient.post()
                .uri(accountServiceUrl + "/accounts/" + accountId + "/credit")
                .body(Map.of("amount", amount))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("Credit failed: " + response.getStatusText());
                })
                .toBodilessEntity();

        log.info("Credit successful: accountId={}", accountId);
    }
}
