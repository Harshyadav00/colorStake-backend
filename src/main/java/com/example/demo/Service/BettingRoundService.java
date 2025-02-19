package com.example.demo.Service;

import com.example.demo.Controller.RoundController;
import com.example.demo.Entity.Dto.RoundResponse;
import com.example.demo.Entity.Entity.Bet;
import com.example.demo.Entity.Entity.BettingRound;
import com.example.demo.Entity.Enum.Colour;
import com.example.demo.Entity.Enum.RoundStatus;
import com.example.demo.Exceptions.RoundNotFoundException;
import com.example.demo.Repository.BettingRoundRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class BettingRoundService {

    @Autowired
    private BettingRoundRepository bettingRoundRepository;

    @Autowired
    private RoundController roundController;

    @Autowired
    private PayoutService payoutService;

    @Autowired
    private AccountService accountService;

    private final Random random = new Random();

    // reference of current round;
    private BettingRound currRound;


    private void setCurrRound(BettingRound currRound) {
        this.currRound = currRound;
    }

    @Scheduled(fixedRate = 150000) // 2 minutes and 30 seconds in milliseconds
    public void createNewRound() {
        LocalDateTime currentTime = LocalDateTime.now();
        BettingRound round = new BettingRound();
        round.setStartTime(currentTime);
        round.setEndTime(currentTime.plusMinutes(2).plusSeconds(30));
        round.setLockTime(currentTime.plusMinutes(2));
        round.setStatus(RoundStatus.ACTIVE);
        round.setTotalBet(0.0);
        round.setBetOnRed(0.0);
        round.setTotalPlayersBetOnRed(0);
        round.setBetOnBlue(0.0);
        round.setTotalPlayersBetOnBlue(0);

        round = bettingRoundRepository.save(round);
        setCurrRound(round);
        roundController.notifyNewRound(round); // Notify clients about the new round
        lockRound(round.getId());
        closeRound(round.getId());
        System.out.println("New betting round created: " + round);
    }

    private void lockRound(String roundId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(120000); // Lock duration
                BettingRound round = bettingRoundRepository.findById(roundId).orElse(null);
                if (round != null) {
                    round.setStatus(RoundStatus.LOCKED);
                    setCurrRound(round);
                    roundController.notifyLockRound(round);
                    bettingRoundRepository.save(round);
                    calculateWinner(round);
                }
            } catch (Exception ex) {
                // Use a logging framework instead of System.out
                System.out.println("Error locking round: " + ex.getMessage());
            }
        });
    }

    private void closeRound(String roundId) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(150000); // Close duration 2.5minutes
                BettingRound round = bettingRoundRepository.findById(roundId).orElse(null);
                if (round != null) {
                    round.setStatus(RoundStatus.CLOSED);
                    bettingRoundRepository.save(round);
                    payoutService.processRoundResults(round);
                    accountService.updateLostResult(round);
                    List<BettingRound> roundsHistory = returnLast10Round();
                    roundController.notifyOfLatestRounds(roundsHistory);
                }
            } catch (Exception ex) {
                // Use a logging framework instead of System.out
                System.out.println("Error closing round: " + ex.getMessage());
            }
        });
    }

    private void calculateWinner(BettingRound round) {
        Pageable pageable = PageRequest.of(0, 10);

        List<BettingRound> history = bettingRoundRepository.findAllByStatus(RoundStatus.CLOSED, pageable).toList();

        Colour winnerColor = determineWinner(round, history);

        round.setWinnerColor(winnerColor);
        bettingRoundRepository.save(round);
    }

    private Colour determineWinner(
            BettingRound currRound,
            List<BettingRound> history) {

        double amountA = currRound.getBetOnBlue();
        double amountB = currRound.getBetOnRed() ;
        int usersA = currRound.getTotalPlayersBetOnBlue() ;
        int usersB = currRound.getTotalPlayersBetOnRed() ;

        double totalAmount = amountA + amountB;
        int totalUsers = usersA + usersB ;

        if (totalAmount == 0 || totalUsers == 0) {
            return random.nextBoolean() ? Colour.RED : Colour.BLUE; // No bets placed, pick randomly
        }

        // Step 1: Compute base probabilities (favor the house)
        double probA = (double) amountB / totalAmount; // Favoring the house
        double probB = (double) amountA / totalAmount;

        // Step 2: Adjust using historical trends
        long last10WinsA = history.stream().filter(color -> color.getWinnerColor().equals(Colour.BLUE)).count();
        long last10WinsB = history.stream().filter(color -> color.getWinnerColor().equals(Colour.RED)).count();

        double trendFactorA = (10.0 - last10WinsA) / 10.0; // More weight if A won less
        double trendFactorB = (10.0 - last10WinsB) / 10.0;

        probA *= trendFactorA;
        probB *= trendFactorB;

        // Step 3: Adjust probability based on number of users
        double userFactorA = (double) usersB / totalUsers; // Fewer users betting on A = Higher chance
        double userFactorB = (double) usersA / totalUsers;

        probA *= userFactorA;
        probB *= userFactorB;

        // Step 4: Normalize probabilities
        double totalProb = probA + probB;
        probA /= totalProb;
        probB /= totalProb;

        // Step 5: Random selection based on adjusted probabilities
        return random.nextDouble() < probA ? Colour.BLUE : Colour.RED ;
    }

    public RoundResponse fetchCurrentRound(){

        BettingRound round = currRound ;

        long timeRemaining = round.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();


        return new RoundResponse(
                round.getId(),
                round.getStartTime(),
                timeRemaining, // Assuming you want to send time remaining in milliseconds
                round.getStatus()
        );
    }

    public void validBettingRound(BettingRound round) throws Exception {

        BettingRound tempRound = bettingRoundRepository.findById(round.getId())
                .orElse(null);

        if (tempRound == null || !tempRound.getStatus().equals(RoundStatus.ACTIVE)) {
            throw new RoundNotFoundException("The bet with ID " + round.getId() + " was not found.");
        }

    }

    @Transactional
    public void addBet(Bet bet) {
        BettingRound round = bettingRoundRepository.findById(bet.getRound().getId()).orElse(null);
        if (bet.getChoice().equals(Colour.RED)) {
            round.setBetOnRed(round.getBetOnRed() + bet.getAmount());
            round.setTotalPlayersBetOnRed(round.getTotalPlayersBetOnRed() + 1);
        } else {
            round.setBetOnBlue(round.getBetOnBlue() + bet.getAmount());
            round.setTotalPlayersBetOnBlue(round.getTotalPlayersBetOnBlue() + 1);
        }
        round.setTotalBet(round.getTotalBet() + bet.getAmount());
    }

    public List<BettingRound> returnLast10Round() {
        Pageable query = PageRequest.of(0, 10, Sort.by("startTime").descending());
        List<BettingRound> latestRounds = bettingRoundRepository.findAllByStatus(RoundStatus.CLOSED, query).toList();
        System.out.println( "Latest Rounds: " + latestRounds);
        return latestRounds;
    }
}
