package com.example.demo.Entity.Entity;

import com.example.demo.Entity.Enum.BetResult;
import com.example.demo.Entity.Enum.Colour;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false) // Foreign key column
    private BettingRound round;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column
    private Account user;
    private Double amount;
    private Colour choice; // e.g., "Red" or "Blue"
    private LocalDateTime placedAt = LocalDateTime.now();
    private BetResult betResult = BetResult.ACTIVE ;

}
