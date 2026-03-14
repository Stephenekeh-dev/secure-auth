package com.steve.secure_auth.repository;

import com.steve.secure_auth.model.MfaSecret;
import com.steve.secure_auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MfaSecretRepository extends JpaRepository<MfaSecret, Long> {
    Optional<MfaSecret> findByUser(User user);
    void deleteByUser(User user);
}