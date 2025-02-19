package com.example.demo.Repository;

import com.example.demo.Entity.Entity.BankTransaction;
import com.example.demo.Entity.Entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    Optional<BankTransaction> findByTxnId(String txnId);

    Page<BankTransaction> findAllByAccountId(String accountId, Pageable pageable);
}
