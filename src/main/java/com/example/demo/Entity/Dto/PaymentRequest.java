package com.example.demo.Entity.Dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String txnId;
    private String amount;
    private String productInfo;
    private String firstName;
    private String email;
    private String phone;
    private String successUrl;
    private String failureUrl;

    // Getters and Setters
}
