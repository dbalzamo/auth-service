package com.fleetpulse.authservice.dto.response;

public record FieldErrorResponse (
    String field,
    String message
){}
