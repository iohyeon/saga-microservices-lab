package com.example.ledgerservice.controller;

import com.example.ledgerservice.dto.BookLedgerRequest;
import com.example.ledgerservice.dto.ReversalRequest;
import com.example.ledgerservice.entity.JournalEntry;
import com.example.ledgerservice.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
@Slf4j
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/book")
    public ResponseEntity<JournalEntry> bookLedger(@RequestBody BookLedgerRequest request) {
        log.info("Book ledger request: {}", request);
        JournalEntry entry = ledgerService.bookLedger(request);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("/reverse")
    public ResponseEntity<Map<String, String>> reverseEntry(@RequestBody ReversalRequest request) {
        log.info("Reverse request: {}", request);
        JournalEntry reversal = ledgerService.reverseEntry(request.getJournalId());
        return ResponseEntity.ok(Map.of(
                "status", "reversed",
                "originalJournalId", request.getJournalId(),
                "reversalJournalId", reversal.getJournalId()
        ));
    }

    @GetMapping("/{journalId}")
    public ResponseEntity<JournalEntry> getEntry(@PathVariable String journalId) {
        JournalEntry entry = ledgerService.getEntry(journalId);
        return ResponseEntity.ok(entry);
    }
}
