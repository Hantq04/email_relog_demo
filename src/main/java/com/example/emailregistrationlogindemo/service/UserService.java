package com.example.emailregistrationlogindemo.service;

import com.example.emailregistrationlogindemo.registration.RegistrationRequest;
import com.example.emailregistrationlogindemo.model.User;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getUsers();
    User registerUser(RegistrationRequest request);
    Optional<User> findByEmail(String email);

    void saveUserVerificationToken(User user, String verificationToken);

    String validateToken(String token);

    String sendResetPasswordEmail(String email) throws MessagingException, UnsupportedEncodingException;

    String resetPassword(String s, String token, String newPassword);

    boolean emailExists(String email);
}
