package com.example.saga.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransferCompletedEvent extends SagaEvent {
    private String message;

    public TransferCompletedEvent(String requestId, String message) {
        super(requestId, LocalDateTime.now(), "TRANSFER_COMPLETED");
        this.message = message;
    }
}
