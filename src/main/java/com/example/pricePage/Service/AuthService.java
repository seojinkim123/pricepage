package com.example.pricePage.Service;


import com.example.pricePage.Dto.LoginRequest;
import com.example.pricePage.Dto.TokenResponse;
import com.example.pricePage.Entity.AuthCredential;
import com.example.pricePage.Entity.AuthProvider;
import com.example.pricePage.Entity.RefreshToken;
import com.example.pricePage.Entity.User;
import com.example.pricePage.Repository.AuthCredentialRepository;
import com.example.pricePage.Repository.RefreshTokenRepository;
import com.example.pricePage.Repository.UserRepository;
import com.example.pricePage.Security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthCredentialRepository authCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /** ID/PW 로그인 */
    @Transactional
    public TokenResponse loginWithPassword(LoginRequest req) {
        AuthCredential cred = authCredentialRepository
                .findByProviderAndProviderUserId(AuthProvider.LOCAL, req.getEmail())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (cred.getPassword() == null || !passwordEncoder.matches(req.getPassword(), cred.getPassword())) {
            throw new RuntimeException("Bad credentials");
        }

        User user = cred.getUser();
        return issueTokens(user);
    }

    /** OAuth2 로그인 성공 시 (provider + sub 기반) */
    @Transactional
    public TokenResponse loginWithOAuth2(AuthProvider provider, String sub, String email, String name) {
        // provider+sub로 credential 찾기
        AuthCredential cred = authCredentialRepository
                .findByProviderAndProviderUserId(provider, sub)
                .orElseGet(() -> {
                    // 계정 생성/연결 규칙:
                    // 1) email이 있으면 기존 User와 연결 시도
                    // 2) 없으면 새 User 생성
                    User user = (email != null)
                            ? userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.create(email, name)))
                            : userRepository.save(User.create(null, name));

                    // 프로필 동기화(선택)
                    user.syncProfile(name);

                    return authCredentialRepository.save(AuthCredential.oauth(user, provider, sub));
                });

        User user = cred.getUser();
        // (선택) 프로필 최신화
        if (name != null) user.syncProfile(name);

        return issueTokens(user);
    }

    /** Refresh 재발급: refresh JWT 검증 + DB 저장값 비교 + 회전 */
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtProvider.validate(refreshToken) || !"refresh".equals(jwtProvider.getType(refreshToken))) {
            throw new RuntimeException("Invalid refresh token");
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        RefreshToken saved = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Refresh not found"));

        if (!saved.getToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh token mismatch");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 회전
        String newAccess = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String newRefresh = jwtProvider.createRefreshToken(user.getId());
        saved.rotate(newRefresh);

        return TokenResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getAccessExpireSeconds())
                .build();
    }

    /** 공통 발급 + Refresh 저장 */
    private TokenResponse issueTokens(User user) {
        String access = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String refresh = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.of(user.getId(), refresh));

        return TokenResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getAccessExpireSeconds())
                .build();
    }
}