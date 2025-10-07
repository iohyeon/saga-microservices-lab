package com.example.riskservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskCheckRequest {
    private String requestId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
}
