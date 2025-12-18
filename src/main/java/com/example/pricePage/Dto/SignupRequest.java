package com.example.pricePage.Dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String name; // 선택 (OAuth2는 name을 주는 경우가 많음)
}
