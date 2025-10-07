package com.example.saga.common.enums;

public enum SagaStatus {
    NEW,
    RISK_CHECK,
    HOLD_FUNDS,
    BOOK_LEDGER,
    COMMIT_FUNDS,
    NOTIFY,
    DONE,
    FAILED,
    COMPENSATING
}