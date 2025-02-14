package com.example.emailregistrationlogindemo.controller;

import com.example.emailregistrationlogindemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ResetPasswordController {
    private final UserService userService;

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        String result = userService.resetPassword(token, password, confirmPassword);
        if ("Password reset successful. You can now log in with your new password.".equals(result)) {
            return "redirect:/login?resetSuccess";
        } else {
            // Truyền lại token vào model để tránh mất token khi reload trang
            model.addAttribute("token", token);
            model.addAttribute("message", result); // Truyền thông báo lỗi về view
            return "reset-password";
        }
    }
}
