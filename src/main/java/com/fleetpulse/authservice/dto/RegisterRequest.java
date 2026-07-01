package com.fleetpulse.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "{validation.email.invalid}")
        @NotBlank(message = "{validation.email.required}")
        String email,

        @NotBlank(message = "{validation.email.required}")
        @Size(min = 4, max = 30, message = "{validation.username.size}")
        String username,

        @NotBlank(message = "{validation.email.required}")
        @Size(min = 8, max = 128, message = "{validation.password.size}")
        String password
) {}