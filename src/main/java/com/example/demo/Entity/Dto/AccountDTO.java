package com.example.demo.Entity.Dto;

import com.example.demo.Entity.Entity.Account;
import lombok.Data;

@Data
public class AccountDTO {
    private String id;
    private String name;
    private String email;
    private Double balance = 0.0;
    private int totalBetPlayed = 0;
    private int totalBetLost = 0;
    private int totalBetWon = 0;

    public AccountDTO (Account account){
        this.id = account.getId();
        this.name  = account.getName();
        this.email = account.getEmail();
        this.balance = account.getBalance();
        this.totalBetPlayed = account.getTotalBetPlayed();
        this.totalBetWon = account.getTotalBetWon();
        this.totalBetLost = account.getTotalBetLost();

    }
}
