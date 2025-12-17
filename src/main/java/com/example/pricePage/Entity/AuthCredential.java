package com.example.pricePage.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "auth_credentials",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerUserId"})
)
public class AuthCredential {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String providerUserId; // LOCAL: email, OAUTH: sub

    private String password; // LOCAL만 (bcrypt)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    public static AuthCredential local(User user, String email, String encodedPassword) {
        AuthCredential ac = new AuthCredential();
        ac.provider = AuthProvider.LOCAL;
        ac.providerUserId = email;
        ac.password = encodedPassword;
        ac.user = user;
        return ac;
    }

    public static AuthCredential oauth(User user, AuthProvider provider, String sub) {
        AuthCredential ac = new AuthCredential();
        ac.provider = provider;
        ac.providerUserId = sub;
        ac.user = user;
        return ac;
    }
}