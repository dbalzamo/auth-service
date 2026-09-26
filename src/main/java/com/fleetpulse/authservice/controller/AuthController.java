package com.fleetpulse.authservice.controller;

import com.fleetpulse.authservice.controller.api.AuthApi;
import com.fleetpulse.authservice.dto.response.AuthResponse;
import com.fleetpulse.authservice.dto.request.LoginRequest;
import com.fleetpulse.authservice.dto.request.RefreshRequest;
import com.fleetpulse.authservice.dto.request.RegisterRequest;
import com.fleetpulse.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Override
    public ResponseEntity<AuthResponse> refresh(RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Override
    public ResponseEntity<Void> logout(RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

}