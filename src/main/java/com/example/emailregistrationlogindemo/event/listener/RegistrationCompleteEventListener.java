package com.example.emailregistrationlogindemo.event.listener;

import com.example.emailregistrationlogindemo.event.RegistrationCompleteEvent;
import com.example.emailregistrationlogindemo.model.User;
import com.example.emailregistrationlogindemo.service.UserService;
import com.example.emailregistrationlogindemo.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {
    private final UserService userService;
    private final EmailService emailService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        User user = event.getUser();
        String verificationToken = UUID.randomUUID().toString();
        userService.saveUserVerificationToken(user, verificationToken);

        String url = event.getApplicationUrl() + "/register/verify-email?token=" + verificationToken;

        try {
            sendVerificationEmail(user, url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Click the link to verify your registration: {}", url);
    }

    private void sendVerificationEmail(User user, String url) throws Exception {
        String subject = "Email Verification";
        String mailContent = String.format(
                "<p>Hi, %s,</p>" +
                        "<p>Thank you for registering with us.</p>" +
                        "<p>Please, follow the link below to complete your registration.</p>" +
                        "<a href=\"%s\">Verify your email to activate your account</a>" +
                        "<p>Thank you <br> Support Team</p>",
                user.getFirstName(), url
        );

        emailService.sendEmail(user.getEmail(), subject, mailContent);
    }
}
