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
public class LedgerServiceClient {

    private final RestClient restClient;
    private final String ledgerServiceUrl;

    public LedgerServiceClient(RestClient.Builder restClientBuilder,
                               @Value("${services.ledger.url}") String ledgerServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.ledgerServiceUrl = ledgerServiceUrl;
    }

    public Map<String, Object> bookLedger(String journalId, String requestId, String debitAccount,
                                          String creditAccount, BigDecimal amount, String description) {
        log.info("Calling Ledger Service: journalId={}, debit={}, credit={}, amount={}",
                journalId, debitAccount, creditAccount, amount);

        Map<String, Object> response = restClient.post()
                .uri(ledgerServiceUrl + "/ledger/book")
                .body(Map.of(
                        "journalId", journalId,
                        "requestId", requestId,
                        "debitAccount", debitAccount,
                        "creditAccount", creditAccount,
                        "amount", amount,
                        "description", description
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, res) -> {
                    throw new RuntimeException("Ledger booking failed: " + res.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, res) -> {
                    throw new RuntimeException("Ledger service error: " + res.getStatusText());
                })
                .body(Map.class);

        log.info("Ledger booked: {}", response);
        return response;
    }

    public Map<String, Object> reverseEntry(String journalId) {
        log.info("Calling Ledger Service to reverse: journalId={}", journalId);

        Map<String, Object> response = restClient.post()
                .uri(ledgerServiceUrl + "/ledger/reverse")
                .body(Map.of("journalId", journalId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, res) -> {
                    throw new RuntimeException("Ledger reversal failed: " + res.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, res) -> {
                    throw new RuntimeException("Ledger service error: " + res.getStatusText());
                })
                .body(Map.class);

        log.info("Ledger reversed: {}", response);
        return response;
    }
}
