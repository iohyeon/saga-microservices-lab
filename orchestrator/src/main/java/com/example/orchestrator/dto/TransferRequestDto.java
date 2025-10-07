package com.example.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String description;
}
