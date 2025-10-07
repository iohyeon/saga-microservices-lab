package com.example.accountservice.controller;

import com.example.accountservice.dto.CommitRequest;
import com.example.accountservice.dto.HoldRequest;
import com.example.accountservice.dto.ReleaseRequest;
import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.AccountHold;
import com.example.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        Account account = accountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/hold")
    public ResponseEntity<AccountHold> holdFunds(@RequestBody HoldRequest request) {
        log.info("Hold request: {}", request);
        AccountHold hold = accountService.holdFunds(
                request.getAccountId(),
                request.getHoldId(),
                request.getAmount()
        );
        return ResponseEntity.ok(hold);
    }

    @PostMapping("/commit")
    public ResponseEntity<Map<String, String>> commitHold(@RequestBody CommitRequest request) {
        log.info("Commit request: {}", request);
        accountService.commitHold(request.getHoldId());
        return ResponseEntity.ok(Map.of("status", "committed", "holdId", request.getHoldId()));
    }

    @PostMapping("/release")
    public ResponseEntity<Map<String, String>> releaseHold(@RequestBody ReleaseRequest request) {
        log.info("Release request: {}", request);
        accountService.releaseHold(request.getHoldId());
        return ResponseEntity.ok(Map.of("status", "released", "holdId", request.getHoldId()));
    }

    @PostMapping("/{accountId}/credit")
    public ResponseEntity<Map<String, String>> creditAccount(
            @PathVariable String accountId,
            @RequestBody Map<String, BigDecimal> request) {
        log.info("Credit request: accountId={}, amount={}", accountId, request.get("amount"));
        accountService.creditAccount(accountId, request.get("amount"));
        return ResponseEntity.ok(Map.of("status", "credited", "accountId", accountId));
    }
}
