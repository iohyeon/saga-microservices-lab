package com.example.orchestrator.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class RiskServiceClient {

    private final RestClient restClient;
    private final String riskServiceUrl;

    public RiskServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${services.risk.url}") String riskServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.riskServiceUrl = riskServiceUrl;
    }

    public Map<String, Object> checkRisk(String requestId, String accountId, BigDecimal amount) {
        log.info("Calling Risk Service: requestId={}, accountId={}, amount={}", requestId, accountId, amount);

        Map<String, Object> response = restClient.post()
                .uri(riskServiceUrl + "/risk/check")
                .body(Map.of(
                        "requestId", requestId,
                        "accountId", accountId,
                        "amount", amount
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, res) -> {
                    throw new RuntimeException("Risk check failed: " + res.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, res) -> {
                    throw new RuntimeException("Risk service error: " + res.getStatusText());
                })
                .body(Map.class);

        log.info("Risk check response: {}", response);

        if ("REJECTED".equals(response.get("status"))) {
            throw new RuntimeException("Risk assessment rejected: " + response.get("reason"));
        }

        return response;
    }
}
