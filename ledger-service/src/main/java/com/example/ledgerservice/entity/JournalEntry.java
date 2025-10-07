package com.example.ledgerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String journalId;

    @Column(nullable = false)
    private String requestId;

    // Debit entry
    private String debitAccount;
    private BigDecimal debitAmount;

    // Credit entry
    private String creditAccount;
    private BigDecimal creditAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status;

    private String description;
    private String reversalJournalId;  // 취소 분개 ID

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EntryStatus {
        POSTED,      // 기록됨
        REVERSED     // 취소됨
    }
}
