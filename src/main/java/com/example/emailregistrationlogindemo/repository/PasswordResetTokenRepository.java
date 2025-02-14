package com.example.emailregistrationlogindemo.repository;

import com.example.emailregistrationlogindemo.model.User;
import com.example.emailregistrationlogindemo.registration.token.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);
}
