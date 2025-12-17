package com.example.pricePage.Dto;


import lombok.Builder;
import lombok.Data;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private long expiresIn;   // seconds
}