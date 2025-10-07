package com.example.accountservice.repository;

import com.example.accountservice.entity.AccountHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountHoldRepository extends JpaRepository<AccountHold, Long> {

    Optional<AccountHold> findByHoldId(String holdId);
}
