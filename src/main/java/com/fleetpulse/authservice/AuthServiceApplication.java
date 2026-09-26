package com.fleetpulse.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto di ingresso del microservizio IAM Provider (auth-service).
 * <p>
 * Questo servizio gestisce l'autenticazione e l'autorizzazione degli utenti
 * tramite JWT (access token) e refresh token. Si occupa di:
 * <ul>
 *   <li>Registrazione nuovi account ({@code POST /api/v1/auth/register})</li>
 *   <li>Login con generazione token ({@code POST /api/v1/auth/login})</li>
 *   <li>Refresh del token ({@code POST /api/v1/auth/refresh})</li>
 *   <li>Logout con revoca dei refresh token ({@code POST /api/v1/auth/logout})</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
