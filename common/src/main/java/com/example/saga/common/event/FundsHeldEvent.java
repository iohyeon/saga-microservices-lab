package com.example.saga.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FundsHeldEvent extends SagaEvent {
    private String holdId;

    public FundsHeldEvent(String requestId, String holdId) {
        super(requestId, LocalDateTime.now(), "FUNDS_HELD");
        this.holdId = holdId;
    }
}
