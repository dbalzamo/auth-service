package com.fleetpulse.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "{validation.refreshToken.required}")
        String refreshToken
) {}