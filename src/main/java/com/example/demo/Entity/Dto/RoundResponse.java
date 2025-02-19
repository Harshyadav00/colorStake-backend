package com.example.demo.Entity.Dto;

import com.example.demo.Entity.Enum.RoundStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RoundResponse {
    private String roundId;
    private LocalDateTime startTime;
    private long timeRemaining;
    private RoundStatus status;


    // Getters and Setters
}
