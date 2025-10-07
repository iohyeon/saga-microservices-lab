package com.example.ledgerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookLedgerRequest {
    private String requestId;
    private String journalId;
    private String debitAccount;
    private String creditAccount;
    private BigDecimal amount;
    private String description;
}
