package com.example.pricePage.Controller;

import com.example.pricePage.Dto.*;
import com.example.pricePage.Service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest dto) {
        return authService.loginWithPassword(dto);
    }

    @PostMapping("/signup")
    public TokenResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }



    @PostMapping("/token/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.getRefreshToken());
    }



}