package com.example.accountservice.service;

import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.AccountHold;
import com.example.accountservice.repository.AccountHoldRepository;
import com.example.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHoldRepository holdRepository;

    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
    }

    @Transactional
    public AccountHold holdFunds(String accountId, String holdId, BigDecimal amount) {
        log.info("Holding funds: accountId={}, holdId={}, amount={}", accountId, holdId, amount);

        // 중복 hold 체크
        if (holdRepository.findByHoldId(holdId).isPresent()) {
            throw new RuntimeException("Hold already exists: " + holdId);
        }

        Account account = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        // 잔액 체크
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Hold 금액 증가
        account.setHeldAmount(account.getHeldAmount().add(amount));
        accountRepository.save(account);

        // Hold 레코드 생성
        AccountHold hold = new AccountHold();
        hold.setAccountId(accountId);
        hold.setHoldId(holdId);
        hold.setAmount(amount);
        hold.setStatus(AccountHold.HoldStatus.HELD);

        return holdRepository.save(hold);
    }

    @Transactional
    public void commitHold(String holdId) {
        log.info("Committing hold: holdId={}", holdId);

        AccountHold hold = holdRepository.findByHoldId(holdId)
                .orElseThrow(() -> new RuntimeException("Hold not found: " + holdId));

        if (hold.getStatus() != AccountHold.HoldStatus.HELD) {
            throw new RuntimeException("Hold already processed: " + holdId);
        }

        Account account = accountRepository.findByAccountIdWithLock(hold.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + hold.getAccountId()));

        // 실제 잔액 차감 & held 금액 감소
        account.setBalance(account.getBalance().subtract(hold.getAmount()));
        account.setHeldAmount(account.getHeldAmount().subtract(hold.getAmount()));
        accountRepository.save(account);

        // Hold 상태 업데이트
        hold.setStatus(AccountHold.HoldStatus.COMMITTED);
        holdRepository.save(hold);
    }

    @Transactional
    public void releaseHold(String holdId) {
        log.info("Releasing hold: holdId={}", holdId);

        AccountHold hold = holdRepository.findByHoldId(holdId)
                .orElseThrow(() -> new RuntimeException("Hold not found: " + holdId));

        if (hold.getStatus() != AccountHold.HoldStatus.HELD) {
            throw new RuntimeException("Hold already processed: " + holdId);
        }

        Account account = accountRepository.findByAccountIdWithLock(hold.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + hold.getAccountId()));

        // Held 금액만 감소 (잔액은 그대로)
        account.setHeldAmount(account.getHeldAmount().subtract(hold.getAmount()));
        accountRepository.save(account);

        // Hold 상태 업데이트
        hold.setStatus(AccountHold.HoldStatus.RELEASED);
        holdRepository.save(hold);
    }

    @Transactional
    public void creditAccount(String accountId, BigDecimal amount) {
        log.info("Crediting account: accountId={}, amount={}", accountId, amount);

        Account account = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }
}
