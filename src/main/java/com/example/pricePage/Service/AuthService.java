package com.example.pricePage.Service;


import com.example.pricePage.Dto.LoginRequest;
import com.example.pricePage.Dto.SignupRequest;
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

        // 1️⃣ email로 User 찾기
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ 이 User의 LOCAL credential 찾기
        AuthCredential cred = authCredentialRepository
                .findByUserAndProvider(user, AuthProvider.LOCAL)
                .orElseThrow(() -> new RuntimeException("이 이메일은 소셜 로그인 계정입니다"));

        // 3️⃣ 비밀번호 검증
        if (!passwordEncoder.matches(req.getPassword(), cred.getPassword())) {
            throw new RuntimeException("비번 틀림");
        }

        // 4️⃣ 토큰 발급
        return issueTokens(user);
    }

    /** ID/PW 회원가입 */
    @Transactional
    public TokenResponse signup(SignupRequest req) {

        // 1️⃣ 이메일 중복 체크 (User 기준)
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 2️⃣ User 생성
        User user = userRepository.save(
                User.create(req.getEmail(), req.getName())
        );

        // 3️⃣ LOCAL AuthCredential 생성
        authCredentialRepository.save(
                AuthCredential.local(
                        user,
                        passwordEncoder.encode(req.getPassword())
                )
        );

        // 4️⃣ 토큰 발급 (선택: 가입 후 바로 로그인)
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
