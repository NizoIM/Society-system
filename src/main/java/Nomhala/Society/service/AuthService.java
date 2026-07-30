package Nomhala.Society.service;

import Nomhala.Society.dto.LoginRequest;
import Nomhala.Society.dto.RegisterRequest;
import Nomhala.Society.entity.Role;
import Nomhala.Society.entity.User;
import Nomhala.Society.repository.UserRepository;
import Nomhala.Society.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ================= OTP =================

    private static class OtpData {

        String otp;
        LocalDateTime expiry;

        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }

    private final Map<String, OtpData> otpStore =
            new ConcurrentHashMap<>();

    // ================= REGISTER =================

    public String register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Optional:
        // If public registration should always be MEMBER:
        // user.setRole(Role.MEMBER);

        user.setRole(
                Role.valueOf(request.getRole().toUpperCase())
        );

        // Require email verification
        user.setEnabled(false);

        String token = UUID.randomUUID().toString();

        user.setVerificationToken(token);

        userRepository.save(user);

        emailService.sendVerificationEmail(user);

        return "Registration successful. Check your email to verify your account.";
    }

    // ================= VERIFY EMAIL =================

    public String verifyEmail(String token) {

        User user = userRepository
                .findByVerificationToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invalid verification link."));

        user.setEnabled(true);

        user.setVerificationToken(null);

        userRepository.save(user);

        return "Email verified successfully.";
    }

    // ================= LOGIN =================

    public Map<String, Object> login(LoginRequest req) {

        User user = userRepository
                .findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException(
                    "Please verify your email before logging in."
            );
        }

        if (!passwordEncoder.matches(
                req.getPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Invalid credentials"
            );
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return Map.of(
                "token", token,
                "role", user.getRole().name(),
                "email", user.getEmail()
        );
    }

    // ================= SEND OTP =================

    public String sendOtp(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String otp =
                String.valueOf(
                        (int)(Math.random()*900000)+100000
                );

        otpStore.put(
                email,
                new OtpData(
                        otp,
                        LocalDateTime.now().plusMinutes(5)
                )
        );

        emailService.sendOtp(email, otp);

        return "OTP sent successfully.";
    }

    // ================= VERIFY OTP =================

    public String verifyOtp(String email, String otp) {

        OtpData data = otpStore.get(email);

        if (data == null)
            return "OTP expired.";

        if (data.expiry.isBefore(LocalDateTime.now())) {

            otpStore.remove(email);

            return "OTP expired.";
        }

        if (!data.otp.equals(otp)) {

            return "Invalid OTP.";
        }

        return "OTP verified.";
    }

    // ================= RESET PASSWORD =================

    public String resetPassword(
            String email,
            String otp,
            String newPassword
    ) {

        OtpData data = otpStore.get(email);

        if (data == null)
            return "Invalid OTP.";

        if (data.expiry.isBefore(LocalDateTime.now())) {

            otpStore.remove(email);

            return "OTP expired.";
        }

        if (!data.otp.equals(otp))
            return "Invalid OTP.";

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        otpStore.remove(email);

        return "Password reset successful.";
    }

}