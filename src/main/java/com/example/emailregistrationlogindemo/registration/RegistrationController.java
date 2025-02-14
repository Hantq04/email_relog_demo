package com.example.emailregistrationlogindemo.registration;

import com.example.emailregistrationlogindemo.exception.UserAlreadyExistsException;
import com.example.emailregistrationlogindemo.model.User;
import com.example.emailregistrationlogindemo.service.UserService;
import com.example.emailregistrationlogindemo.event.RegistrationCompleteEvent;
import com.example.emailregistrationlogindemo.registration.token.VerificationToken;
import com.example.emailregistrationlogindemo.repository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;

//    @PostMapping
//    public String registerUser(@Validated @RequestBody RegistrationRequest registrationRequest, final HttpServletRequest request) {
//        User user = userService.registerUser(registrationRequest);
//        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
//        return "Successfully! Please, check your email to complete your registration";
//    }

    @PostMapping
    public String registerUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            RegistrationRequest registrationRequest = new RegistrationRequest(firstName, lastName, email, password, role);
            User user = userService.registerUser(registrationRequest);
            publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));

            redirectAttributes.addFlashAttribute("success", "Registration successful! Please check your email to verify.");
            return "redirect:/login";
        } catch (UserAlreadyExistsException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }

//    @GetMapping("/verify-email")
//    public String verifyEmail(@RequestParam("token") String token) {
//        VerificationToken theToken = tokenRepository.findByToken(token);
//
//        if (theToken == null) {  // 🔹 Nếu token không tồn tại
//            return "Invalid verification token";
//        }
//
//        if (theToken.getUser().isEnabled()) {
//            return "This account has already been verified. Please, login.";
//        }
//
//        String verificationResult = userService.validateToken(token);
//        if (verificationResult.equalsIgnoreCase("valid")) {
//            return "Email verified successfully. Now you can login to your account";
//        }
//        return "Invalid verification token";
//    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        VerificationToken theToken = tokenRepository.findByToken(token);

        if (theToken == null) {
            redirectAttributes.addFlashAttribute("error", "invalidToken");
            return "redirect:/login";
        }

        if (theToken.getUser().isEnabled()) {
            redirectAttributes.addFlashAttribute("error", "alreadyVerified");
            return "redirect:/login";
        }

        String verificationResult = userService.validateToken(token);
        if (verificationResult.equalsIgnoreCase("valid")) {
            redirectAttributes.addFlashAttribute("success", "verified");
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("error", "invalidToken");
        return "redirect:/login";
    }



    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    @GetMapping
    public String showRegistrationPage() {
        return "registration"; // Trả về file registration.html trong templates
    }
}
