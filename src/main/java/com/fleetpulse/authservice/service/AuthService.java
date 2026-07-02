package com.fleetpulse.authservice.service;

import com.fleetpulse.authservice.dto.response.AuthResponse;
import com.fleetpulse.authservice.dto.request.LoginRequest;
import com.fleetpulse.authservice.dto.request.RefreshRequest;
import com.fleetpulse.authservice.dto.request.RegisterRequest;
import com.fleetpulse.authservice.enums.TypeRole;
import com.fleetpulse.authservice.exception.AuthException;
import com.fleetpulse.authservice.mapper.AccountMapper;
import com.fleetpulse.authservice.model.Account;
import com.fleetpulse.authservice.model.RefreshToken;
import com.fleetpulse.authservice.model.Role;
import com.fleetpulse.authservice.repository.AccountRepository;
import com.fleetpulse.authservice.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final MessageSource messageSource;
    private final AccountMapper accountMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new AuthException(HttpStatus.CONFLICT, "error.email.already-registered");
        }

        Role defaultRole = roleRepository.findByRoleName(TypeRole.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(
                        messageSource.getMessage("error.role.customer-missing", null, LocaleContextHolder.getLocale())
                ));

        Account account = accountMapper.toEntity(request);
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setRole(defaultRole);

        accountRepository.save(account);

        return issueTokens(account);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Account account = accountRepository.findByEmailOrUsername(request.username())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "error.account.not-found"));

        return issueTokens(account);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken newRefreshToken = refreshTokenService.rotate(request.refreshToken());
        Account account = newRefreshToken.getAccount();
        String accessToken = jwtService.generateAccessToken(account);

        return AuthResponse.of(accessToken, newRefreshToken.getToken(), jwtService.getAccessTokenExpirationMs());
    }

    @Transactional
    public void logout(RefreshRequest request) {
        RefreshToken token = refreshTokenService.rotate(request.refreshToken());
        refreshTokenService.revokeAllForUser(token.getAccount());
    }

    private AuthResponse issueTokens(Account account) {
        String accessToken = jwtService.generateAccessToken(account);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(account);
        return AuthResponse.of(accessToken, refreshToken.getToken(), jwtService.getAccessTokenExpirationMs());
    }

}