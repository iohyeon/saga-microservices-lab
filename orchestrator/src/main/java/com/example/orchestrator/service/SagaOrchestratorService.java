package com.example.orchestrator.service;

import com.example.orchestrator.client.AccountServiceClient;
import com.example.orchestrator.client.RiskServiceClient;
import com.example.orchestrator.client.LedgerServiceClient;
import com.example.orchestrator.client.NotificationServiceClient;
import com.example.orchestrator.dto.TransferRequestDto;
import com.example.orchestrator.entity.SagaInstance;
import com.example.orchestrator.repository.SagaInstanceRepository;
import com.example.saga.common.enums.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorService {

    private final SagaInstanceRepository sagaRepository;
    private final AccountServiceClient accountClient;
    private final RiskServiceClient riskClient;
    private final LedgerServiceClient ledgerClient;
    private final NotificationServiceClient notificationClient;

    @Transactional
    public SagaInstance initiateTransfer(TransferRequestDto request) {
        log.info("Initiating transfer: from={}, to={}, amount={}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // 1. Create Saga instance
        String sagaId = "SAGA-" + UUID.randomUUID().toString();
        String holdId = "HOLD-" + UUID.randomUUID().toString();
        String journalId = "JRN-" + UUID.randomUUID().toString();

        SagaInstance saga = new SagaInstance();
        saga.setSagaId(sagaId);
        saga.setStatus(SagaStatus.NEW);
        saga.setFromAccountId(request.getFromAccountId());
        saga.setToAccountId(request.getToAccountId());
        saga.setAmount(request.getAmount());
        saga.setDescription(request.getDescription());
        saga.setHoldId(holdId);
        saga.setJournalId(journalId);

        saga = sagaRepository.save(saga);
        log.info("Saga created: sagaId={}", sagaId);

        // 2. Execute Saga steps
        try {
            executeTransferSaga(saga);
        } catch (Exception e) {
            log.error("Saga failed: sagaId={}, error={}", sagaId, e.getMessage(), e);
            compensate(saga, e.getMessage());
        }

        return sagaRepository.findById(saga.getId()).orElseThrow();
    }

    private void executeTransferSaga(SagaInstance saga) {
        log.info("Executing Saga: sagaId={}", saga.getSagaId());

        // Step 1: Risk check
        updateStatus(saga, SagaStatus.RISK_CHECK);
        Map<String, Object> riskResult = riskClient.checkRisk(
                saga.getSagaId(),
                saga.getFromAccountId(),
                saga.getAmount()
        );
        log.info("Step 1 completed: Risk check passed - {}", riskResult);

        // Step 2: Hold funds
        updateStatus(saga, SagaStatus.HOLD_FUNDS);
        accountClient.holdFunds(saga.getFromAccountId(), saga.getHoldId(), saga.getAmount());
        log.info("Step 2 completed: Funds held");

        // Step 3: Book ledger
        updateStatus(saga, SagaStatus.BOOK_LEDGER);
        Map<String, Object> ledgerResult = ledgerClient.bookLedger(
                saga.getJournalId(),
                saga.getSagaId(),
                saga.getFromAccountId(),
                saga.getToAccountId(),
                saga.getAmount(),
                saga.getDescription() != null ? saga.getDescription() : "Transfer from " + saga.getFromAccountId() + " to " + saga.getToAccountId()
        );
        log.info("Step 3 completed: Ledger booked - {}", ledgerResult);

        // Step 4: Commit hold (deduct balance)
        updateStatus(saga, SagaStatus.COMMIT_FUNDS);
        accountClient.commitHold(saga.getHoldId());
        log.info("Step 4 completed: Funds committed");

        // Step 5: Credit to receiver
        accountClient.creditAccount(saga.getToAccountId(), saga.getAmount());
        log.info("Step 5 completed: Receiver credited");

        // Step 6: Send notification
        updateStatus(saga, SagaStatus.NOTIFY);
        notificationClient.sendNotification(
                saga.getSagaId(),
                saga.getFromAccountId(),
                "TRANSFER_SUCCESS",
                String.format("Transfer of %s from %s to %s completed successfully",
                        saga.getAmount(), saga.getFromAccountId(), saga.getToAccountId())
        );
        log.info("Step 6 completed: Notification sent");

        // Success
        updateStatus(saga, SagaStatus.DONE);
        log.info("Saga completed successfully: sagaId={}", saga.getSagaId());
    }

    private void compensate(SagaInstance saga, String errorMessage) {
        log.warn("Starting compensation: sagaId={}, currentStatus={}", saga.getSagaId(), saga.getStatus());

        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setErrorMessage(errorMessage);
        sagaRepository.save(saga);

        try {
            // Reverse ledger if it was booked
            if (saga.getJournalId() != null &&
                (saga.getStatus() == SagaStatus.BOOK_LEDGER ||
                 saga.getStatus() == SagaStatus.COMMIT_FUNDS ||
                 saga.getStatus() == SagaStatus.NOTIFY ||
                 saga.getStatus() == SagaStatus.COMPENSATING)) {
                log.info("Reversing ledger: journalId={}", saga.getJournalId());
                try {
                    ledgerClient.reverseEntry(saga.getJournalId());
                } catch (Exception e) {
                    log.warn("Ledger reversal failed (may not exist yet): {}", e.getMessage());
                }
            }

            // Release hold if it exists
            if (saga.getHoldId() != null) {
                log.info("Releasing hold: holdId={}", saga.getHoldId());
                try {
                    accountClient.releaseHold(saga.getHoldId());
                } catch (Exception e) {
                    log.warn("Hold release failed (may not exist or already released): {}", e.getMessage());
                }
            }

            // Send failure notification
            try {
                notificationClient.sendNotification(
                        saga.getSagaId(),
                        saga.getFromAccountId(),
                        "TRANSFER_FAILED",
                        String.format("Transfer of %s from %s to %s failed: %s",
                                saga.getAmount(), saga.getFromAccountId(), saga.getToAccountId(), errorMessage)
                );
            } catch (Exception e) {
                log.warn("Failure notification could not be sent: {}", e.getMessage());
            }

            saga.setStatus(SagaStatus.FAILED);
            sagaRepository.save(saga);
            log.info("Compensation completed: sagaId={}", saga.getSagaId());

        } catch (Exception e) {
            log.error("Compensation failed: sagaId={}, error={}", saga.getSagaId(), e.getMessage(), e);
            saga.setErrorMessage(saga.getErrorMessage() + " | Compensation error: " + e.getMessage());
            saga.setStatus(SagaStatus.FAILED);
            sagaRepository.save(saga);
        }
    }

    private void updateStatus(SagaInstance saga, SagaStatus status) {
        saga.setStatus(status);
        sagaRepository.save(saga);
        log.info("Saga status updated: sagaId={}, status={}", saga.getSagaId(), status);
    }

    @Transactional(readOnly = true)
    public SagaInstance getSaga(String sagaId) {
        return sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));
    }
}
