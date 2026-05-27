package Nomhala.Society.controller;

import Nomhala.Society.dto.LoginRequest;
import Nomhala.Society.dto.RegisterRequest;
import Nomhala.Society.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

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
    public String register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
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
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword
    ) {
        return authService.resetPassword(email, otp, newPassword);
    }
}