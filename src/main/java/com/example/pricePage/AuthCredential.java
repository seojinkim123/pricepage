package com.example.pricePage;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class AuthCredential {


    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    // LOCAL, GOOGLE, KAKAO, APPLE

    private String providerUserId;
    // LOCAL이면 email or username
    // OAUTH면 sub

    private String password; // LOCAL 전용 (bcrypt)

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;


}
