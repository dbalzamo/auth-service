package com.fleetpulse.authservice.repository;

import com.fleetpulse.authservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("FROM Account WHERE username = :value OR email = :value")
    Optional<Account> findByEmailOrUsername(@Param("value") String value);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}