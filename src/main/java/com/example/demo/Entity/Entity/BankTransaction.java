package com.example.demo.Entity.Entity;

import com.example.demo.Entity.Enum.BankTransactionType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class BankTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;  // The user's account

    private Double amount;

    @Enumerated(EnumType.STRING)
    private BankTransactionType type; // DEPOSIT or PAYOUT

    private String source;  // Example: "PayU" for deposits, "User Bank" for payouts
    private String destination; // Example: "User Account" for deposits, "Bank Account" for payouts

    private String status; // PENDING, SUCCESS, FAILED
    private LocalDateTime timestamp = LocalDateTime.now();

    private String txnId ;

    // Constructors, Getters, and Setters
}

