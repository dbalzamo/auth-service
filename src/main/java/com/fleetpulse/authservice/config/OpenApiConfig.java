package com.fleetpulse.authservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "IAM PROVIDER",
                description = "AUTHENTICATION API REST",
                version = "v1"
        )
)
public class OpenApiConfig {

    /**
     * Configura il server URL dell'OpenAPI spec per puntare al gateway (8080)
     * invece che direttamente all'auth-service (8081).
     * <p>
     * Questo e' necessario perche' Swagger UI carica lo spec dal gateway
     * (tramite /iam/v3/api-docs) e deve eseguire le chiamate REST passando
     * sempre dal gateway, non direttamente al backend.
     * </p>
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080/iam"));
    }

}