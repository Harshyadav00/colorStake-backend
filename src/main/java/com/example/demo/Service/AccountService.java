package com.example.demo.Service;

import com.example.demo.Entity.Dto.AccountDTO;
import com.example.demo.Entity.Dto.RegisterRequest;
import com.example.demo.Entity.Entity.Account;
import com.example.demo.Entity.Entity.Bet;
import com.example.demo.Entity.Entity.BettingRound;
import com.example.demo.Entity.Enum.BetResult;
import com.example.demo.Entity.Enum.Colour;
import com.example.demo.Exceptions.AmountNotFoundException;
import com.example.demo.Exceptions.UserNotFoundException;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.BetRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BetRepository betRepository;

    public void isValidAccount(Account account, Double amount) throws Exception {
        Account userAccount = accountRepository.findById(account.getId()).orElse(null);

        if (userAccount == null)
            throw new UserNotFoundException("Invalid Account! with accountID : " + account.getId());

        if (amount < 0 || amount > userAccount.getBalance() || amount % 1 > 0)
            throw new AmountNotFoundException("Invalid Amount");
    }

    public void deductAmount(Account user, Double amount) throws Exception {
        Account userAccount = accountRepository.findById(user.getId()).orElse(null);

        if (amount > userAccount.getBalance())
            throw new AmountNotFoundException("Invalid Amount");
        userAccount.setTotalBetPlayed(userAccount.getTotalBetPlayed()+1);
        userAccount.setBalance(userAccount.getBalance() - amount);

        accountRepository.save(userAccount);

    }

    public void createAccount(RegisterRequest user) {
        Account account = new Account();
        account.setName(user.getName());
        account.setEmail(user.getEmail());
        accountRepository.save(account);
    }

    public Optional<Account> getUserByEmail(@NotBlank @Email String email)  {
        return accountRepository.findByEmail(email);
    }

    public double getBalanceByUser(Account user) {
        return accountRepository.getBalanceById(user.getId());
    }

    public AccountDTO fetchAccountData(String uuid) throws Exception{
        Account account = accountRepository.findById(uuid).orElseThrow(() -> new Exception("Invalid User"));
        return new AccountDTO(account);
    }

    public void updateLostResult(BettingRound round) {
        Colour color = Colour.RED == round.getWinnerColor() ? Colour.BLUE : Colour.RED ;
        List<Bet> lostBets = betRepository.findByRoundIdAndChoice(round.getId(),color );

        for (Bet bet : lostBets) {
            Account user = bet.getUser();

            user.setTotalBetLost(user.getTotalBetLost()+1);

            bet.setBetResult(BetResult.LOST);
            betRepository.save(bet);

            accountRepository.save(user);

        }

    }
}
