package com.example.demo.Entity.Dto;

import com.example.demo.Entity.Entity.Bet;
import com.example.demo.Entity.Enum.BetResult;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BetDTO {
    private String id;
    private String roundId;
    private String userId;
    private Double amount;
    private String choice;
    private BetResult betResult;
    private LocalDateTime placedAt;

    public BetDTO(Bet bet) {
        this.id = bet.getId();
        this.roundId = bet.getRound().getId(); // Only ID
        this.userId = bet.getUser().getId(); // Only ID
        this.amount = bet.getAmount();
        this.choice = bet.getChoice().toString();
        this.placedAt = bet.getPlacedAt();
        this.betResult = bet.getBetResult();
    }

    // Getters & Setters
}
