package com.example.saga.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LedgerBookedEvent extends SagaEvent {
    private String journalId;

    public LedgerBookedEvent(String requestId, String journalId) {
        super(requestId, LocalDateTime.now(), "LEDGER_BOOKED");
        this.journalId = journalId;
    }
}
