package com.fleetpulse.authservice.controller.api;

import com.fleetpulse.authservice.dto.AuthResponse;
import com.fleetpulse.authservice.dto.LoginRequest;
import com.fleetpulse.authservice.dto.RefreshRequest;
import com.fleetpulse.authservice.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.fleetpulse.authservice.util.SwaggerConstant.*;

@Tag(name = "Auth", description = "Endpoint di autenticazione e registrazione")
@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @Operation(
            description = DESCRIPTION_REGISTER,
            summary = SUMMARY_REGISTER,
            tags = {IDENTITY_AUTH_MANAGEMENT_CONTROLLER}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = SWAGGER_STATUS_200, description = SUCCESS),
            @ApiResponse(responseCode = SWAGGER_STATUS_400, description = BAD_REQUEST),
            @ApiResponse(responseCode = SWAGGER_STATUS_401, description = UNAUTHORIZED),
            @ApiResponse(responseCode = SWAGGER_STATUS_500, description = ERROR)
    })
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    );


    @Operation(
            description = DESCRIPTION_LOGIN,
            summary = SUMMARY_LOGIN,
            tags = {IDENTITY_AUTH_MANAGEMENT_CONTROLLER}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = SWAGGER_STATUS_200, description = SUCCESS),
            @ApiResponse(responseCode = SWAGGER_STATUS_400, description = BAD_REQUEST),
            @ApiResponse(responseCode = SWAGGER_STATUS_401, description = UNAUTHORIZED),
            @ApiResponse(responseCode = SWAGGER_STATUS_500, description = ERROR)
    })
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    );


    @Operation(
            description = DESCRIPTION_REFRESH,
            summary = SUMMARY_REFRESH,
            tags = {IDENTITY_AUTH_MANAGEMENT_CONTROLLER}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = SWAGGER_STATUS_200, description = SUCCESS),
            @ApiResponse(responseCode = SWAGGER_STATUS_400, description = BAD_REQUEST),
            @ApiResponse(responseCode = SWAGGER_STATUS_401, description = UNAUTHORIZED),
            @ApiResponse(responseCode = SWAGGER_STATUS_500, description = ERROR)
    })
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    );


    @Operation(
            description = DESCRIPTION_LOGOUT,
            summary = SUMMARY_LOGOUT,
            tags = {IDENTITY_AUTH_MANAGEMENT_CONTROLLER}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = SWAGGER_STATUS_200, description = SUCCESS),
            @ApiResponse(responseCode = SWAGGER_STATUS_400, description = BAD_REQUEST),
            @ApiResponse(responseCode = SWAGGER_STATUS_401, description = UNAUTHORIZED),
            @ApiResponse(responseCode = SWAGGER_STATUS_500, description = ERROR)
    })
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshRequest request
    );
}