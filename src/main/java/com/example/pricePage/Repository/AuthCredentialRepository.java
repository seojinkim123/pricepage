package com.example.pricePage.Repository;

import com.example.pricePage.Entity.AuthCredential;
import com.example.pricePage.Entity.AuthProvider;
import com.example.pricePage.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthCredentialRepository extends JpaRepository<AuthCredential,Long> {
    Optional<AuthCredential> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    Optional<AuthCredential> findByUserAndProvider(User user, AuthProvider provider);
}
