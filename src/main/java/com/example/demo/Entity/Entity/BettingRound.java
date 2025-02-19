package com.example.demo.Entity.Entity;

import com.example.demo.Entity.Enum.Colour;
import com.example.demo.Entity.Enum.RoundStatus;
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
@Table(name = "betting_rounds")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class BettingRound {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lockTime;
    private RoundStatus status;

    private Colour winnerColor;
    private Double totalBet; // total amount of bet
    private Double betOnRed; // total amount on red
    private int totalPlayersBetOnRed; // total no. of players on Red
    private Double betOnBlue; // total amount on blue
    private int totalPlayersBetOnBlue; // total no. of players on Blue

    // Getters and Setters
}