package com.example.riskservice.dto;

import com.example.riskservice.entity.RiskAssessment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskCheckResponse {
    private String requestId;
    private RiskAssessment.RiskStatus status;
    private String riskScore;
    private String reason;
}
