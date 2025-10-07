package com.example.orchestrator.dto;

import com.example.saga.common.enums.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDto {
    private String sagaId;
    private SagaStatus status;
    private String message;
}
