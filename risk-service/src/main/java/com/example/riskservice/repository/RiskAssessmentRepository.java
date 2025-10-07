package com.example.riskservice.repository;

import com.example.riskservice.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    Optional<RiskAssessment> findByRequestId(String requestId);
}
