package com.fleetpulse.authservice.exception;

import com.fleetpulse.authservice.dto.response.ErrorResponse;
import com.fleetpulse.authservice.dto.response.FieldErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final MessageSource messageSource;


  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, Locale locale) {
    log.warn("AuthException: {}", ex.getMessage());

    String msg = messageSource.getMessage(ex.getMessage(), null, locale);

    return ResponseEntity
            .status(ex.getStatus())
            .body(new ErrorResponse("AUTH_ERROR", msg,null ,ex.getStatus().value(), Instant.now()));
  }


  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, Locale locale) {
    log.warn("Bad credentials attempt: {}", ex.getMessage());

    String msg = messageSource.getMessage("error.credentials.invalid", null, locale);

    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("INVALID_CREDENTIALS", msg, null ,HttpStatus.UNAUTHORIZED.value(), Instant.now()));
  }


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining(", "));

    List<FieldErrorResponse>  fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

    return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", null, fieldErrors, 400, Instant.now()));
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, Locale locale) {
    log.error("Unhandled exception", ex); // <-- fondamentale: logga lo stacktrace completo

    String msg = messageSource.getMessage("error.internal", null, locale);

    return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_ERROR", msg, null,  500, Instant.now()));
  }


}