package com.fleetpulse.authservice.dto.response;

/**
 * DTO di risposta per le operazioni di autenticazione.
 * <p>
 * Contiene i token restituiti al client dopo login o registrazione:
 * <ul>
 *   <li>{@code accessToken} — JWT da allegare nell'header {@code Authorization}</li>
 *   <li>{@code refreshToken} — UUID per ottenere un nuovo access token senza rifare login</li>
 *   <li>{@code tokenType} — sempre {@code Bearer}</li>
 *   <li>{@code expiresIn} — secondi di validita' dell'access token</li>
 * </ul>
 * </p>
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInMs){
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInMs / 1000);
    }
}
