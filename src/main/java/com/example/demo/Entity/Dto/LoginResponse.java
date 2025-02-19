package com.example.demo.Entity.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String Id;
    private String name;
    private String email;
    private Double balance;
    private String accessToken;
    private String refreshToken;
}
