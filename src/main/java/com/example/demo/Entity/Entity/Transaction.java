package com.example.demo.Entity.Entity;

import com.example.demo.Entity.Enum.TransactionType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column
    private Account user;

    private double amount;
    private TransactionType type; // "WINNING_PAYOUT", "BET_PLACED", etc.

    private LocalDateTime timestamp = LocalDateTime.now();

    public Transaction(Account user, double amount, TransactionType type) {
        this.user = user;
        this.amount = amount;
        this.type = type;
    }
}
