package com.fleetpulse.authservice.service;

import com.fleetpulse.authservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementazione di {@link UserDetailsService} per Spring Security.
 * <p>
 * Quando un utente fa login, Spring Security chiama questo servizio per
 * caricare i dati dell'utente dal database (username, password cifrata, ruoli).
 * I dati vengono poi usati da {@code DaoAuthenticationProvider} per verificare
 * la password.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountRepository accountRepository;


    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return accountRepository.findByEmailOrUsername(username).orElseThrow(() -> new UsernameNotFoundException("error.account.not-found"));
    }
}
