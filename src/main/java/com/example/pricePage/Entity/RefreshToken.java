package com.example.pricePage.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String token;

    public static RefreshToken of(Long userId, String token) {
        RefreshToken rt = new RefreshToken();
        rt.userId = userId;
        rt.token = token;
        return rt;
    }

    public void rotate(String newToken) {
        this.token = newToken;
    }
}