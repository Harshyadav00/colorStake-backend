package com.example.demo.Controller;

import com.example.demo.Entity.Entity.BettingRound;
import com.example.demo.Entity.Dto.RoundResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.ZoneId;
import java.util.List;

@Controller
public class RoundController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyNewRound(BettingRound round) {
        System.out.println("Starting new Round");
        // Calculate time remaining in milliseconds
        long timeRemaining = round.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();

        // Create a message with round details
        RoundResponse message = new RoundResponse(
                round.getId(),
                round.getStartTime(),
                timeRemaining, // Assuming you want to send time remaining in milliseconds
                round.getStatus()
        );

        // Send message to all subscribers
        messagingTemplate.convertAndSend("/topic/newRound", message);
    }

    public void notifyLockRound(BettingRound round) {
        System.out.println("Locking the round");
        // Calculate time remaining in milliseconds
        long timeRemaining = round.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();

        // Create a message with round details
        RoundResponse message = new RoundResponse(
                round.getId(),
                round.getStartTime(),
                timeRemaining, // Assuming you want to send time remaining in milliseconds
                round.getStatus()
        );

        // Send message to all subscribers
        messagingTemplate.convertAndSend("/topic/lockRound", message);

    }

    public void notifyOfLatestRounds(List<BettingRound> latestRounds) {
        messagingTemplate.convertAndSend("/topic/latestRounds", latestRounds);
    }

    public void sendBalanceUpdate(String userId, double newBalance) {
        messagingTemplate.convertAndSend("/topic/balance-update/" + userId, newBalance);
    }

}
