package com.example.orchestrator.repository;

import com.example.orchestrator.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {
    Optional<SagaInstance> findBySagaId(String sagaId);
}
