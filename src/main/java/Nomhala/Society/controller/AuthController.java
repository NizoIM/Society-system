package Nomhala.Society.controller;

import Nomhala.Society.dto.LoginRequest;
import Nomhala.Society.dto.RegisterRequest;
import Nomhala.Society.entity.User;
import Nomhala.Society.repository.UserRepository;
import Nomhala.Society.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;
    private UserRepository userRepository;

    // =========================
    // LOGIN (returns JWT)
    // =========================
    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    // =========================
    // REGISTER USER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authService.register(request)
        );
    }
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email) {
        return authService.sendOtp(email);
    }
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp) {
        return authService.verifyOtp(email, otp);
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {

        User user = userRepository
                .findByVerificationToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invalid verification link"));

        user.setEnabled(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        return ResponseEntity.ok(
                "Your email has been verified successfully. You can now log in."
        );
    }
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword
    ) {
        return authService.resetPassword(email, otp, newPassword);
    }
}