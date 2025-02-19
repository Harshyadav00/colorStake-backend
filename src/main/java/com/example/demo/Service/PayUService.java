package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayUService {

    private final RestTemplate restTemplate = new RestTemplate();


    @Value("${payu.merchant.key}")
    private String merchantKey;

    @Value("${payu.merchant.salt}")
    private String merchantSalt;

    @Autowired
    private PayoutService payoutService;

    public String verifyTransaction(String transactionId) throws Exception {
        String command = "verify_payment";
        String tempHash = merchantKey + "|" + command + "|" + transactionId + "|" + merchantSalt; // Implement hash logic
        String hash = HashGenerator.getSHA512(tempHash);

        String url = "https://test.payu.in/merchant/postservice?form=2";

        // Prepare request body
        String requestBody = "key=" + merchantKey +
                "&command=" + command +
                "&var1=" + transactionId +
                "&hash=" + hash;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(response.getBody() );
        System.out.println(transactionId);
        String res = handleVerifyTransactionResponse(response.getBody(), transactionId);
        return res ;

    }

    public String handleVerifyTransactionResponse(String jsonResponse, String transactionId) throws Exception {
        try {
            JSONObject responseJson = new JSONObject(jsonResponse);

            // Check if status is 1 (successful verification)
            if (responseJson.getInt("status") == 1) {
                JSONObject transactionDetails = responseJson.getJSONObject("transaction_details");

                // Check if transactionId exists in response
                if (transactionDetails.has(transactionId)) {
                    JSONObject transaction = transactionDetails.getJSONObject(transactionId);

                    // Verify if the transaction status is "success"
                    if ("success".equalsIgnoreCase(transaction.getString("status"))) {
                        double amount = transaction.getDouble("net_amount_debit");
                        payoutService.handlePaymentSuccess(transactionId, amount);
                        return "Payment Successfull" ;
                    } else {
                        payoutService.updatePaymentFailed(transactionId);
                        throw new Exception("Payment Failed");
                    }
                } else {
                    System.out.println("Transaction ID not found in response");
                }
            } else {
                System.out.println("Transaction verification failed. Status: " + responseJson.getInt("status"));
            }
        } catch (JSONException e) {
            payoutService.updatePaymentFailed(transactionId);
            System.out.println("Error parsing JSON response: " + e.getMessage());
        }
        return "Payment Failed";
    }


    private String generateHash(String merchantKey, String command, String transactionId) {
        // Implement PayU hash generation logic
        return "generated_hash"; // Replace this with actual hash logic
    }
}
