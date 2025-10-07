package com.example.orchestrator.service;

import com.example.orchestrator.client.AccountServiceClient;
import com.example.orchestrator.dto.TransferRequestDto;
import com.example.orchestrator.entity.SagaInstance;
import com.example.orchestrator.repository.SagaInstanceRepository;
import com.example.saga.common.enums.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorService {

    private final SagaInstanceRepository sagaRepository;
    private final AccountServiceClient accountClient;

    @Transactional
    public SagaInstance initiateTransfer(TransferRequestDto request) {
        log.info("Initiating transfer: from={}, to={}, amount={}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // 1. Create Saga instance
        String sagaId = "SAGA-" + UUID.randomUUID().toString();
        String holdId = "HOLD-" + UUID.randomUUID().toString();

        SagaInstance saga = new SagaInstance();
        saga.setSagaId(sagaId);
        saga.setStatus(SagaStatus.NEW);
        saga.setFromAccountId(request.getFromAccountId());
        saga.setToAccountId(request.getToAccountId());
        saga.setAmount(request.getAmount());
        saga.setDescription(request.getDescription());
        saga.setHoldId(holdId);

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

        // Step 1: Hold funds
        updateStatus(saga, SagaStatus.HOLD_FUNDS);
        accountClient.holdFunds(saga.getFromAccountId(), saga.getHoldId(), saga.getAmount());
        log.info("Step 1 completed: Funds held");

        // Step 2: Commit hold (deduct balance)
        updateStatus(saga, SagaStatus.COMMIT_FUNDS);
        accountClient.commitHold(saga.getHoldId());
        log.info("Step 2 completed: Funds committed");

        // Step 3: Credit to receiver
        updateStatus(saga, SagaStatus.NOTIFY);
        accountClient.creditAccount(saga.getToAccountId(), saga.getAmount());
        log.info("Step 3 completed: Receiver credited");

        // Success
        updateStatus(saga, SagaStatus.DONE);
        log.info("Saga completed successfully: sagaId={}", saga.getSagaId());
    }

    private void compensate(SagaInstance saga, String errorMessage) {
        log.warn("Starting compensation: sagaId={}", saga.getSagaId());

        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setErrorMessage(errorMessage);
        sagaRepository.save(saga);

        try {
            // Release hold if it exists
            if (saga.getHoldId() != null &&
                (saga.getStatus() == SagaStatus.HOLD_FUNDS ||
                 saga.getStatus() == SagaStatus.COMPENSATING)) {
                log.info("Releasing hold: holdId={}", saga.getHoldId());
                accountClient.releaseHold(saga.getHoldId());
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
