package com.example.ledgerservice.repository;

import com.example.ledgerservice.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    Optional<JournalEntry> findByJournalId(String journalId);
    Optional<JournalEntry> findByRequestId(String requestId);
}
