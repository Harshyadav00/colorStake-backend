package com.example.demo.Entity.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique request ID

    @Column(nullable = false)
    private String userEmail ;

    @Column(nullable = false)
    private Double amount; // Withdrawal amount

    @Column(nullable = false)
    private String otp; // Correct OTP for verification

    @Column(nullable = false)
    private LocalDateTime otpExpiry; // OTP expiration time

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Request creation timestamp

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.otpExpiry = createdAt.plusMinutes(10); // OTP expires in 10 minutes
    }
}

