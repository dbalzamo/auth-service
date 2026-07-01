package com.fleetpulse.authservice.exception;

import com.fleetpulse.authservice.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends RuntimeException {

  private final MessageSource messageSource;


  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
    log.warn("AuthException: {}", ex.getMessage());
    String msg = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());
    return ResponseEntity
            .status(ex.getStatus())
            .body(new ErrorResponse("AUTH_ERROR", msg, ex.getStatus().value(), Instant.now()));
  }


  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
    log.warn("Bad credentials attempt: {}", ex.getMessage());
    String msg = messageSource.getMessage("error.credentials.invalid", null, LocaleContextHolder.getLocale());
    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("INVALID_CREDENTIALS", msg, HttpStatus.UNAUTHORIZED.value(), Instant.now()));
  }


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining(", "));

    return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", msg, 400, Instant.now()));
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unhandled exception", ex); // <-- fondamentale: logga lo stacktrace completo
    String msg = messageSource.getMessage("error.internal", null, LocaleContextHolder.getLocale());

    return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_ERROR", msg, 500, Instant.now()));
  }


}