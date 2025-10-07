package com.example.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldRequest {
    private String accountId;
    private String holdId;
    private BigDecimal amount;
}
