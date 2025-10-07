package com.example.saga.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RiskApprovedEvent extends SagaEvent {
    private String riskCheckId;

    public RiskApprovedEvent(String requestId) {
        super(requestId, LocalDateTime.now(), "RISK_APPROVED");
    }
}
