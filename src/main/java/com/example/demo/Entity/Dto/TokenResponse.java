package com.example.demo.Entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;



    @Override
    public String toString() {
        return "accessToken: " + accessToken + "/nrefreshToken: " + refreshToken;
    }
}