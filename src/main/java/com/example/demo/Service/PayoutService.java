package com.example.demo.Service;

import com.example.demo.Controller.RoundController;
import com.example.demo.Entity.Dto.PaymentRequest;
import com.example.demo.Entity.Entity.*;
import com.example.demo.Entity.Enum.BankTransactionType;
import com.example.demo.Entity.Enum.BetResult;
import com.example.demo.Entity.Enum.Colour;
import com.example.demo.Entity.Enum.TransactionType;
import com.example.demo.Entity.Entity.WithdrawalRequest;
import com.example.demo.Exceptions.AmountNotFoundException;
import com.example.demo.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PayoutService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private RoundController roundController;

    @Value("${payu.merchant.key}")
    private String merchantKey;

    @Value("${payu.merchant.salt}")
    private String merchantSalt;

    @Value("${payu.api.url}")
    private String payuUrl;

    @Autowired
    private BankTransactionRepository bankTransactionRepository ;

    @Autowired
    private WithdrawalRepository withdrawalRepository ;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void processRoundResults(BettingRound round) {
        Colour winningColor = round.getWinnerColor();
        if (winningColor == null) return;

        // Fetch all bets for the winning color
        List<Bet> winningBets = betRepository.findByRoundIdAndChoice(round.getId(), winningColor);

        // Process payouts
        for (Bet bet : winningBets) {
            Account user = bet.getUser();
            double winnings = bet.getAmount() * 1.9; // Example: 2x payout
            user.setTotalBetWon(user.getTotalBetWon()+1);

            bet.setBetResult(BetResult.WON);
            betRepository.save(bet);

            // Update user's balance
            user.setBalance(user.getBalance() + winnings);
            accountRepository.save(user);

            // Record transaction
            Transaction transaction = new Transaction(user, winnings, TransactionType.WINNING_PAYOUT);
            transactionRepository.save(transaction);

            // Notify user
            roundController.sendBalanceUpdate(user.getId(), user.getBalance());
        }
    }

    public Map<String, String> createPaymentRecord(PaymentRequest request) throws Exception {
        try {
            String txnId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

            String hash = HashGenerator.generateHash(merchantKey, txnId, request.getAmount(),
                    request.getProductInfo(), request.getFirstName(), request.getEmail(), merchantSalt);

            // Fetch user account
            Account account = accountRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User account not found"));


            // Store initial transaction as PENDING
            BankTransaction transaction = new BankTransaction();
            transaction.setAccount(account);
            transaction.setAmount(Double.parseDouble( request.getAmount()));
            transaction.setType(BankTransactionType.DEPOSIT);
            transaction.setSource("PayU");
            transaction.setDestination("User Account");
            transaction.setStatus("PENDING");
            transaction.setTxnId(txnId);
            bankTransactionRepository.save(transaction);


            Map<String, String> response = getStringStringMap(request, txnId, hash);

            return response;
        } catch (Exception ex) {
            throw new Exception("Something Went Wrong");
        }
    }

    public String updatePaymentFailed(String txnId) {
        BankTransaction transaction = bankTransactionRepository.findByTxnId(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Update transaction status
        transaction.setStatus("FAILED");
        bankTransactionRepository.save(transaction);
        return "Payment failed and recorded";

    }

    public String handlePaymentSuccess(String txnid, Double amount) {
        BankTransaction transaction = bankTransactionRepository.findByTxnId(txnid)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Verify it's a deposit
        if (transaction.getType() != BankTransactionType.DEPOSIT) {
            throw new RuntimeException("Invalid transaction type");
        }

        if("SUCCESS".equals(transaction.getStatus()))
            return "Payment Already added";

        // Update transaction status
        transaction.setStatus("SUCCESS");
        bankTransactionRepository.save(transaction);

        // Update user balance
        Account account = transaction.getAccount();
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        roundController.sendBalanceUpdate(account.getId(), account.getBalance());


        return "Payment successful and recorded";
    }


    private Map<String, String> getStringStringMap(PaymentRequest request, String txnId, String hash) {
        Map<String, String> response = new HashMap<>();
        response.put("payuUrl", payuUrl);
        response.put("key", merchantKey);
        response.put("txnid", txnId);
        response.put("amount", request.getAmount());
        response.put("productinfo", request.getProductInfo());
        response.put("firstname", request.getFirstName());
        response.put("email", request.getEmail());
        response.put("phone", request.getPhone());
        response.put("surl", "https://colorstake-backend-production.up.railway.app/api/payments/callback"); // Success URL
        response.put("furl", "https://colorstake-backend-production.up.railway.app/api/payments/callback"); // Failure URL
        response.put("hash", hash);
        return response;
    }

    public Page<BankTransaction> fetchAllTransactionsByUserId(String userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("timestamp")));
        Page<BankTransaction> transactions = bankTransactionRepository.findAllByAccountId(userId, pageable);

        return transactions ;


    }

    public void withdrawMoney(WithdrawalRequest request) throws AmountNotFoundException, Exception {

        // Retrieve the account balance
        double accountBalance = accountRepository.getBalanceByEmail(request.getUserEmail());

        // Check if account balance is sufficient
        if (accountBalance < request.getAmount()) {
            throw new AmountNotFoundException("Insufficient balance");
        }

        // Check if a withdrawal request already exists, and delete it if found
        withdrawalRepository.findByUserEmail(request.getUserEmail()).ifPresent(withdrawalRepository::delete);


        // Generate a 6-digit OTP
        String otp = generateOtp();

        // Set OTP and expiry time (e.g., expiry in 5 minutes)
        request.setOtp(otp);

        // Save request to the database
        withdrawalRepository.save(request);

        // Send OTP to the user (via email)
        try {
            sendOTPEmail(request.getUserEmail(), otp);  // Ensure this is implemented correctly
        } catch (Exception e) {
            // Handle any failure in sending OTP (email service down, etc.)
            throw new Exception("Failed to send OTP. Please try again later.");
        }
    }


    // Method to send OTP email
    private void sendOTPEmail(String userEmail, String otp) {
        String to = userEmail ;
        String subject = "Your OTP Code" ;
        String text = "Your OTP code is: " + otp;

        // Send the email
        emailService.sendEmail(to, subject, text);

        System.out.println("OTP email sent to " + userEmail);
    }

    // Helper method to generate a 6-digit OTP
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates a number between 100000-999999
        return String.valueOf(otp);
    }

    public void verifyWithdrawal(WithdrawalRequest request) throws Exception {

        WithdrawalRequest withdrawalRequest = withdrawalRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new Exception("Invalid request"));

        LocalDateTime now = LocalDateTime.now();
        if(withdrawalRequest.getOtpExpiry().isBefore(now))
            throw new Exception("Request Expired!, Request again");

        if(!withdrawalRequest.getOtp().equals(request.getOtp()))
            throw new Exception("Incorrect otp");

        Account userAccount = accountRepository.findByEmail(withdrawalRequest.getUserEmail())
                .orElseThrow(() -> new Exception("Invalid User"));
        userAccount.setBalance(userAccount.getBalance() - withdrawalRequest.getAmount());
        accountRepository.save(userAccount);
        String txnId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);


        BankTransaction bankTransaction = new BankTransaction() ;
        bankTransaction.setAmount(withdrawalRequest.getAmount());
        bankTransaction.setAccount(userAccount);
        bankTransaction.setSource("Payu");
        bankTransaction.setDestination("User Account");
        bankTransaction.setStatus("SUCCESS");
        bankTransaction.setType(BankTransactionType.PAYOUT);
        bankTransaction.setTxnId(txnId);

        bankTransactionRepository.save(bankTransaction);

        roundController.sendBalanceUpdate(userAccount.getId(), userAccount.getBalance());

        withdrawalRepository.delete(withdrawalRequest);



    }
}

