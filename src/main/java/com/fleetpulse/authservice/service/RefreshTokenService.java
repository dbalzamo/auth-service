package com.fleetpulse.authservice.service;

import com.fleetpulse.authservice.config.JwtProperties;
import com.fleetpulse.authservice.exception.AuthException;
import com.fleetpulse.authservice.model.Account;
import com.fleetpulse.authservice.model.RefreshToken;
import com.fleetpulse.authservice.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshToken createRefreshToken(Account account) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .account(account)
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotate(String oldTokenValue) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenValue)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "error.refreshToken.invalid"));

        if (oldToken.isRevoked()) {
            // Riuso di un token già ruotato: possibile furto. Revoca tutto per sicurezza.
            refreshTokenRepository.revokeAllByUser(oldToken.getAccount());
            throw new AuthException(HttpStatus.UNAUTHORIZED, "error.refreshToken.compromised");
        }

        if (oldToken.isExpired()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "error.refreshToken.expired");
        }

        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return createRefreshToken(oldToken.getAccount());
    }

    @Transactional
    public void revokeAllForUser(Account account) {
        refreshTokenRepository.revokeAllByUser(account);
    }
}