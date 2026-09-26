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

/**
 * Servizio per la gestione dei refresh token.
 * <p>
 * Il refresh token e' un UUID memorizzato nel database (non e' un JWT).
 * Viene usato per ottenere un nuovo access token senza richiedere
 * all'utente di reinserire le credenziali.
 * </p>
 * <p>
 * Caratteristiche di sicurezza:
 * <ul>
 *   <li><b>Rotation</b> — ogni volta che un refresh token viene usato, ne viene
 *       generato uno nuovo e il vecchio viene revocato</li>
 *   <li><b>Reuse detection</b> — se un token gia' ruotato viene riutilizzato,
 *       tutti i refresh token dell'utente vengono revocati (possibile furto)</li>
 *   <li><b>Scadenza</b> — configurabile via {@code security.jwt.refresh-token-expiration-ms}</li>
 * </ul>
 * </p>
 */
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

    /**
     * Ruota un refresh token: revoca il vecchio e ne crea uno nuovo.
     * Se il token vecchio era gia' stato revocato (riutilizzo), revoca
     * tutti i token dell'utente per prevenire accessi non autorizzati.
     */
    @Transactional
    public RefreshToken rotate(String oldTokenValue) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenValue)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "error.refreshToken.invalid"));

        if (oldToken.isRevoked()) {
            // Riuso di un token gia' ruotato: possibile furto. Revoca tutto per sicurezza.
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
