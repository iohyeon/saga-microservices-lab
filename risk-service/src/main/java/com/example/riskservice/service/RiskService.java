package com.example.riskservice.service;

import com.example.riskservice.dto.RiskCheckRequest;
import com.example.riskservice.entity.RiskAssessment;
import com.example.riskservice.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final RiskAssessmentRepository repository;

    @Transactional
    public RiskAssessment checkRisk(RiskCheckRequest request) {
        log.info("Risk check: requestId={}, amount={}", request.getRequestId(), request.getAmount());

        // 멱등성 체크
        if (repository.findByRequestId(request.getRequestId()).isPresent()) {
            log.warn("Duplicate risk check: requestId={}", request.getRequestId());
            return repository.findByRequestId(request.getRequestId()).get();
        }

        RiskAssessment assessment = new RiskAssessment();
        assessment.setRequestId(request.getRequestId());
        assessment.setFromAccountId(request.getFromAccountId());
        assessment.setToAccountId(request.getToAccountId());
        assessment.setAmount(request.getAmount());

        // 간단한 리스크 룰
        boolean approved = evaluateRisk(request);

        if (approved) {
            assessment.setStatus(RiskAssessment.RiskStatus.APPROVED);
            assessment.setRiskScore("LOW");
            assessment.setReason("Transfer approved");
            log.info("Risk approved: requestId={}", request.getRequestId());
        } else {
            assessment.setStatus(RiskAssessment.RiskStatus.REJECTED);
            assessment.setRiskScore("HIGH");
            assessment.setReason("Amount exceeds limit or suspicious pattern");
            log.warn("Risk rejected: requestId={}", request.getRequestId());
        }

        return repository.save(assessment);
    }

    private boolean evaluateRisk(RiskCheckRequest request) {
        // 간단한 룰: 50000원 이상은 거부
        if (request.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            return false;
        }

        // 실제로는 여기서 AML, 이상거래 탐지 등 수행
        // 예: 블랙리스트 체크, 거래 패턴 분석, 한도 체크 등

        return true;
    }

    @Transactional(readOnly = true)
    public RiskAssessment getAssessment(String requestId) {
        return repository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Risk assessment not found: " + requestId));
    }
}
