// src/main/java/com/steve/secure_auth/service/RefreshTokenService.java
package com.steve.secure_auth.service;

import com.steve.secure_auth.model.RefreshToken;
import com.steve.secure_auth.model.User;
import com.steve.secure_auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
     public RefreshTokenService(RefreshTokenRepository refreshTokenRepository){
         this.refreshTokenRepository=refreshTokenRepository;
     }
    public RefreshToken create(User user) {
        // delete existing tokens for this user (optional policy)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));
        return refreshTokenRepository.save(rt);
    }

    public RefreshToken validate(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw new IllegalArgumentException("Refresh token expired");
        }
        return rt;
    }

    public void revokeByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void revokeByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}
