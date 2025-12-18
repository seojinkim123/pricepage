package com.example.pricePage.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "auth_credentials",
        uniqueConstraints = {
                // OAuth 전용 유니크 (LOCAL은 providerUserId = null)
                @UniqueConstraint(columnNames = {"provider", "providerUserId"}),
                // 한 User는 같은 provider를 하나만 가짐
                @UniqueConstraint(columnNames = {"user_id", "provider"})
        }
)
public class AuthCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 인증 방식 (LOCAL / GOOGLE / KAKAO / NAVER ...) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    /**
     * 외부 Provider 사용자 ID
     * - OAuth2: sub / id / response.id
     * - LOCAL: null (의미 없음)
     */
    @Column
    private String providerUserId;

    /**
     * 비밀번호 (LOCAL 전용)
     * - OAuth2: null
     */
    @Column
    private String password;

    /** 이 인증 수단이 속한 User */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /* =========================
       Factory Methods
       ========================= */

    /** LOCAL (ID/PW) */
    public static AuthCredential local(User user, String encodedPassword) {
        AuthCredential ac = new AuthCredential();
        ac.provider = AuthProvider.LOCAL;
        ac.password = encodedPassword;
        ac.user = user;
        return ac;
    }

    /** OAuth2 (Google / Kakao / Naver ...) */
    public static AuthCredential oauth(User user, AuthProvider provider, String providerUserId) {
        AuthCredential ac = new AuthCredential();
        ac.provider = provider;
        ac.providerUserId = providerUserId;
        ac.user = user;
        return ac;
    }

    /* =========================
       Domain Logic
       ========================= */

    public boolean isLocal() {
        return provider == AuthProvider.LOCAL;
    }

    public void changePassword(String encodedPassword) {
        if (!isLocal()) {
            throw new IllegalStateException("OAuth credential cannot have password");
        }
        this.password = encodedPassword;
    }
}