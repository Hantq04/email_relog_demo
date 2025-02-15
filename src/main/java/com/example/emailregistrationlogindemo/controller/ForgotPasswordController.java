package com.example.emailregistrationlogindemo.controller;

import com.example.emailregistrationlogindemo.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {
    private final UserService userService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) throws MessagingException, UnsupportedEncodingException {
        String result = userService.sendResetPasswordEmail(email);
        model.addAttribute("message", result);
        return "forgot-password";
    }

}
