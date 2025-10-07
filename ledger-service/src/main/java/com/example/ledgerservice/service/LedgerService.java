package com.example.ledgerservice.service;

import com.example.ledgerservice.dto.BookLedgerRequest;
import com.example.ledgerservice.entity.JournalEntry;
import com.example.ledgerservice.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final JournalEntryRepository repository;

    @Transactional
    public JournalEntry bookLedger(BookLedgerRequest request) {
        log.info("Booking ledger: journalId={}, debit={}, credit={}, amount={}",
                request.getJournalId(), request.getDebitAccount(), request.getCreditAccount(), request.getAmount());

        // 멱등성 체크
        if (repository.findByJournalId(request.getJournalId()).isPresent()) {
            log.warn("Duplicate booking: journalId={}", request.getJournalId());
            return repository.findByJournalId(request.getJournalId()).get();
        }

        JournalEntry entry = new JournalEntry();
        entry.setJournalId(request.getJournalId());
        entry.setRequestId(request.getRequestId());
        entry.setDebitAccount(request.getDebitAccount());
        entry.setDebitAmount(request.getAmount());
        entry.setCreditAccount(request.getCreditAccount());
        entry.setCreditAmount(request.getAmount());
        entry.setStatus(JournalEntry.EntryStatus.POSTED);
        entry.setDescription(request.getDescription());

        JournalEntry saved = repository.save(entry);
        log.info("Ledger booked: journalId={}", saved.getJournalId());

        return saved;
    }

    @Transactional
    public JournalEntry reverseEntry(String journalId) {
        log.info("Reversing ledger entry: journalId={}", journalId);

        JournalEntry original = repository.findByJournalId(journalId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found: " + journalId));

        if (original.getStatus() == JournalEntry.EntryStatus.REVERSED) {
            log.warn("Entry already reversed: journalId={}", journalId);
            return original;
        }

        // 역분개 생성 (Debit <-> Credit 반대)
        String reversalId = "REV-" + UUID.randomUUID().toString();

        JournalEntry reversal = new JournalEntry();
        reversal.setJournalId(reversalId);
        reversal.setRequestId(original.getRequestId());
        reversal.setDebitAccount(original.getCreditAccount());  // 반대
        reversal.setDebitAmount(original.getCreditAmount());
        reversal.setCreditAccount(original.getDebitAccount());  // 반대
        reversal.setCreditAmount(original.getDebitAmount());
        reversal.setStatus(JournalEntry.EntryStatus.POSTED);
        reversal.setDescription("Reversal of " + journalId);

        repository.save(reversal);

        // 원본 엔트리 상태 업데이트
        original.setStatus(JournalEntry.EntryStatus.REVERSED);
        original.setReversalJournalId(reversalId);
        repository.save(original);

        log.info("Ledger reversed: original={}, reversal={}", journalId, reversalId);

        return reversal;
    }

    @Transactional(readOnly = true)
    public JournalEntry getEntry(String journalId) {
        return repository.findByJournalId(journalId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found: " + journalId));
    }
}
