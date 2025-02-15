package com.example.emailregistrationlogindemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            Model model) {
        if (error != null) {
            switch (error) {
                case "notVerified":
                    model.addAttribute("error", "Your account has not been verified. Please check your email.");
                    break;
                case "invalidToken":
                    model.addAttribute("error", "Invalid verification token.");
                    break;
                case "alreadyVerified":
                    model.addAttribute("error", "This account has already been verified. Please login.");
                    break;
                default:
                    model.addAttribute("error", "Login failed. Invalid username or password.");
            }
        }
        if ("verified".equals(success)) {
            model.addAttribute("success", "Email verified successfully! You can now login.");
        }
        return "login";
    }
}
