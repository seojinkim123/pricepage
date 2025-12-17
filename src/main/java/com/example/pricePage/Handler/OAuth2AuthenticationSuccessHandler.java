package com.example.pricePage.Handler;

import com.example.pricePage.Dto.TokenResponse;
import com.example.pricePage.Entity.AuthProvider;
import com.example.pricePage.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.oauth2.redirect-success-url}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Google 기준: sub는 "sub", email/name은 표준 클레임
        String sub = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        TokenResponse tokens = authService.loginWithOAuth2(AuthProvider.GOOGLE, sub, email, name);

        // 웹에서는 보통 쿠키로 내려줌(예시)
        addCookie(response, "accessToken", tokens.getAccessToken(), 60 * 60);
        addCookie(response, "refreshToken", tokens.getRefreshToken(), 60 * 60 * 24 * 14);

        response.sendRedirect(successRedirectUrl);
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAgeSec) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS면 true
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSec);
        response.addCookie(cookie);
    }
}