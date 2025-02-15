package com.example.emailregistrationlogindemo.service;

import com.example.emailregistrationlogindemo.model.User;
import com.example.emailregistrationlogindemo.registration.RegistrationRequest;
import com.example.emailregistrationlogindemo.repository.UserRepository;
import com.example.emailregistrationlogindemo.exception.UserAlreadyExistsException;
import com.example.emailregistrationlogindemo.registration.token.PasswordResetToken;
import com.example.emailregistrationlogindemo.registration.token.VerificationToken;
import com.example.emailregistrationlogindemo.repository.PasswordResetTokenRepository;
import com.example.emailregistrationlogindemo.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegistrationRequest request) {
        Optional<User> user = userRepository.findByEmail(request.email());
        if (user.isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }
        var newUser = new User();
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(request.role());
        return userRepository.save(newUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public String validateToken(String theToken) {
        VerificationToken token = tokenRepository.findByToken(theToken);
        if (token == null) {
            return "Invalid verification token";
        }
        User user = token.getUser();
        Calendar calendar = Calendar.getInstance();
        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
            tokenRepository.delete(token);
            return "Your token already expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public void saveUserVerificationToken(User user, String token) {
        var verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);
    }

    public String sendResetPasswordEmail(String email) throws MessagingException, UnsupportedEncodingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Kiểm tra xem user đã có token chưa
        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUser(user);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken;
        if (existingToken.isPresent()) {
            // Nếu token đã tồn tại, cập nhật token mới
            resetToken = existingToken.get();
            resetToken.setToken(token);
            resetToken.setExpirationTime(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 phút
        } else {
            // Nếu chưa có, tạo token mới
            resetToken = new PasswordResetToken(token, user);
        }
        passwordResetTokenRepository.save(resetToken);
        // Gửi email đặt lại mật khẩu
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "Reset Password",
                "<a href=\"" + resetUrl + "\">Click here to reset your password</a>");
        return "Check your email for the password reset link";
    }

//    @Transactional
//    public String resetPassword(String token, String newPassword) {
//        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
//        if (resetToken == null || resetToken.getExpirationTime().before(new java.util.Date())) {
//            return "Invalid or expired token";
//        }
//        User user = resetToken.getUser();
//        userRepository.updatePasswordByEmail(user.getEmail(), passwordEncoder.encode(newPassword));
//        passwordResetTokenRepository.delete(resetToken);
//        return "Password reset successful. You can now log in with your new password.";
//    }

    @Transactional
    public String resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return "Passwords do not match!";
        }
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpirationTime().before(new Date())) {
            return "Invalid or expired token";
        }
        User user = resetToken.getUser();
        userRepository.updatePasswordByEmail(user.getEmail(), passwordEncoder.encode(newPassword));
        passwordResetTokenRepository.delete(resetToken);
        return "Password reset successful. You can now log in with your new password.";
    }
}
