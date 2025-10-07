package com.example.orchestrator.controller;

import com.example.orchestrator.dto.TransferRequestDto;
import com.example.orchestrator.dto.TransferResponseDto;
import com.example.orchestrator.entity.SagaInstance;
import com.example.orchestrator.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sagas")
@RequiredArgsConstructor
@Slf4j
public class SagaController {

    private final SagaOrchestratorService orchestratorService;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> initiateTransfer(@RequestBody TransferRequestDto request) {
        log.info("Transfer request received: {}", request);

        SagaInstance saga = orchestratorService.initiateTransfer(request);

        TransferResponseDto response = new TransferResponseDto(
                saga.getSagaId(),
                saga.getStatus(),
                saga.getErrorMessage() != null ? saga.getErrorMessage() : "Transfer processed"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sagaId}")
    public ResponseEntity<SagaInstance> getSaga(@PathVariable String sagaId) {
        SagaInstance saga = orchestratorService.getSaga(sagaId);
        return ResponseEntity.ok(saga);
    }
}
