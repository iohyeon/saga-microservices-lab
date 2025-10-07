package com.example.orchestrator.entity;

import com.example.saga.common.enums.SagaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "saga_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sagaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    // Transfer details
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String description;

    // Hold tracking
    private String holdId;

    // Ledger tracking
    private String journalId;

    // Error tracking
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SagaStatus.NEW;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
