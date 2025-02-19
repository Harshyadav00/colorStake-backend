package com.example.demo.Service;

import com.example.demo.Controller.RoundController;
import com.example.demo.Entity.Entity.Bet;
import com.example.demo.Entity.Dto.BetDTO;
import com.example.demo.Entity.Entity.Transaction;
import com.example.demo.Entity.Enum.TransactionType;
import com.example.demo.Repository.BetRepository;
import com.example.demo.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BetService {

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private BettingRoundService bettingRoundService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RoundController roundController;

    // logic for placing bet ;
    public String placeBet(Bet bet) throws Exception {
        try {
            bettingRoundService.validBettingRound(bet.getRound());
            accountService.isValidAccount(bet.getUser(), bet.getAmount());

            accountService.deductAmount(bet.getUser(), bet.getAmount());
            betRepository.save(bet);

            Transaction transaction = new Transaction(bet.getUser(), bet.getAmount(), TransactionType.BET_PLACED);
            transactionRepository.save(transaction);

            bettingRoundService.addBet(bet);

            double balance = accountService.getBalanceByUser(bet.getUser());
            roundController.sendBalanceUpdate(bet.getUser().getId(), balance);


            return "Bet Placed";

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    public Page<BetDTO> fetchAllBetsByUserId(String userId, int page, int size) {
//        System.out.println("userId: " + userId+ " page: " + page + " size: " + size );
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("placedAt")));
        Page<Bet> bets = betRepository.findAllByUserId(userId, pageable);
        System.out.println("bets: " +bets);
        Page<BetDTO> res = bets.map(BetDTO::new); // Converts each Bet to BetDTO
        System.out.println("res: " + res);
        return  res;
    }

}
