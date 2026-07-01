package com.fleetpulse.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        Long accessTokenExpirationMs,
        Long refreshTokenExpirationMs
)
{}