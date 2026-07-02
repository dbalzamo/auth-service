package com.fleetpulse.authservice.dto.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String error,
        String message,
        List<FieldErrorResponse>  fieldErrors,
        int status,
        Instant timestamp
) {}