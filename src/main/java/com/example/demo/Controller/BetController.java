package com.example.demo.Controller;

import com.example.demo.Entity.Entity.Bet;
import com.example.demo.Entity.Dto.BetDTO;
import com.example.demo.Exceptions.AmountNotFoundException;
import com.example.demo.Exceptions.RoundNotFoundException;
import com.example.demo.Exceptions.UserNotFoundException;
import com.example.demo.Service.BetService;
import com.example.demo.Service.BettingRoundService;
import com.example.demo.Utility.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bet")
public class BetController {

    @Autowired
    private BetService betService;

    @Autowired
    private BettingRoundService bettingRoundService;

    @PostMapping("/placeBet")
    public ResponseEntity<?> placeBet(@RequestBody Bet bet) throws Exception {
        try {
            System.out.println("Betting placing in action: " + bet);
            String res = betService.placeBet(bet);
            return ResponseEntity.ok(new ApiResponse<>(true, res, ""));
        } catch (AmountNotFoundException ex) {
            // Insufficient balance
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, null, ex.getMessage()));
        } catch (RoundNotFoundException ex) {
            // Invalid round details
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, null, "Invalid Round Details"));
        } catch (UserNotFoundException ex) {
            // User not found
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, null, "User Not Found"));
        } catch (Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, null, "An unexpected error occurred."));
        }
    }

    @GetMapping("/fetchAllBetsByUserId")
    public ResponseEntity<?> fetchAllBetsByUserId(
            @RequestParam String userId,
            @RequestParam int page,
            @RequestParam int size) {

        Page<BetDTO> bets = betService.fetchAllBetsByUserId(userId, page, size);
        return ResponseEntity.ok(bets);
    }


    // method for fetching curr Round
    @GetMapping("/fetchCurrentRound")
    public ResponseEntity<?> fetchCurrentRound() {
        return ResponseEntity.ok(bettingRoundService.fetchCurrentRound());
    }

    @GetMapping("/fetchRoundHistory")
    public ResponseEntity<?> fetchRoundHistory() {
        return ResponseEntity.ok(bettingRoundService.returnLast10Round());
    }

}
