package com.example.pricePage.Entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    public static User create(String email, String name) {
        User u = new User();
        u.email = email;
        u.name = name;
        u.role = Role.ROLE_USER;
        return u;
    }

    public void syncProfile(String name) {
        this.name = name;
    }
}