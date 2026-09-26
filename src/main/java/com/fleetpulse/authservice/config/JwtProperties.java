package com.fleetpulse.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mappa le proprieta' {@code security.jtx.*} da application.yaml in un record.
 * <p>
 * Proprieta' gestite:
 * <ul>
 *   <li>{@code secret} — chiave segreta Base64 per firmare i JWT</li>
 *   <li>{@code accessTokenExpirationMs} — durata access token in millisecondi</li>
 *   <li>{@code refreshTokenExpirationMs} — durata refresh token in millisecondi</li>
 * </ul>
 * </p>
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        Long accessTokenExpirationMs,
        Long refreshTokenExpirationMs
)
{}
