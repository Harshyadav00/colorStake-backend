package com.example.demo.Controller;

import com.example.demo.Entity.Dto.PaymentRequest;
import com.example.demo.Entity.Entity.BankTransaction;
import com.example.demo.Entity.Entity.WithdrawalRequest;
import com.example.demo.Service.PayUService;
import com.example.demo.Service.PayoutService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PayoutService payoutService;

    @Autowired
    private PayUService payUService;

    @PostMapping("/create")
    public Map<String, String> createPayment(@RequestBody PaymentRequest request) {
        try {
            return payoutService.createPaymentRecord(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestParam String txnId) {
        try {
            String res  = payUService.verifyTransaction(txnId);
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Payment Failed");
        }
    }

    @CrossOrigin(origins = "null") // Allow `null` origins only for callback
    @PostMapping("/callback")
    public void handlePayUCallback(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        // Log the received data
        System.out.println("Payment Data: " + params);

        // Extract necessary fields
        String txnId = params.get("txnid");
        String status = params.get("status");

        // Redirect to React frontend with query parameters
        String redirectUrl = "https://colorstake.netlify.app/account?txnId=" + txnId + "&status=" + status;
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/fetchAllTransactionsByUserId")
    public ResponseEntity<?> fetchAllTransactionsByUserId(
            @RequestParam String userId,
            @RequestParam int page,
            @RequestParam int size) {

            Page<BankTransaction> transactions = payoutService.fetchAllTransactionsByUserId(userId, page, size);
            return ResponseEntity.ok(transactions);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withDrawMoney(@RequestBody WithdrawalRequest request) {
        try {
            payoutService.withdrawMoney(request);
            return ResponseEntity.ok("Otp sent to mail");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/verifyWithdrawal")
    public ResponseEntity<?> verifyWithdrawal(@RequestBody WithdrawalRequest request) {
        try {
            payoutService.verifyWithdrawal(request);
            return ResponseEntity.ok("Withdrawal successfull");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
