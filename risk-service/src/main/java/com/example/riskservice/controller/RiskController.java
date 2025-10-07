package com.example.riskservice.controller;

import com.example.riskservice.dto.RiskCheckRequest;
import com.example.riskservice.dto.RiskCheckResponse;
import com.example.riskservice.entity.RiskAssessment;
import com.example.riskservice.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
@Slf4j
public class RiskController {

    private final RiskService riskService;

    @PostMapping("/check")
    public ResponseEntity<RiskCheckResponse> checkRisk(@RequestBody RiskCheckRequest request) {
        log.info("Risk check request: {}", request);

        RiskAssessment assessment = riskService.checkRisk(request);

        RiskCheckResponse response = new RiskCheckResponse(
                assessment.getRequestId(),
                assessment.getStatus(),
                assessment.getRiskScore(),
                assessment.getReason()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RiskAssessment> getAssessment(@PathVariable String requestId) {
        RiskAssessment assessment = riskService.getAssessment(requestId);
        return ResponseEntity.ok(assessment);
    }
}
